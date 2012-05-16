﻿using System;
using System.Diagnostics;
using System.IO;
using log4net;

namespace LMWrapper.LISpMiner
{
	public abstract class Executable
	{
		private static readonly ILog Log = LogManager.GetLogger(typeof(Executable));

		private readonly Stopwatch _stopwatch;

		public LISpMiner LISpMiner { get; set; }

		public string ApplicationName { get; protected set; }

		public ExecutableStatus Status { get; protected set; }

		public virtual string Dsn { get; set; }

		public string LMPath { get; set; }

		/// <summary>
		/// /Quiet    ... errors reported to _AppLog.dat instead on screen
		/// </summary>
		public bool Quiet { get; set; }

		/// <summary>
		/// /NoProgress   ... no progress dialog is displayed
		/// </summary>
		public bool NoProgress { get; set; }		

		/// <summary>
		/// /AppLog:[log_file]		... (O) alternative path and file name for logging
		/// </summary>
		public string AppLog { get; set; }

		public abstract string Arguments { get; }

		protected Executable()
		{
			this.Quiet = true;
			this.NoProgress = true;
			this.Status = ExecutableStatus.Ready;
			this._stopwatch = new Stopwatch();
		}

		public void Execute()
		{
			var errorFilePath = String.IsNullOrEmpty(this.AppLog) ? String.Format("{0}/_AppLog.dat", this.LMPath) : String.Format("{0}/{1}", this.LMPath, this.AppLog);

			this.Run();

			if(File.Exists(errorFilePath))
			{
				var message = File.ReadAllText(errorFilePath);
				File.Delete(errorFilePath);
				throw new LISpMinerException(message);
			}
		}

		protected virtual void Run()
		{
			var p = new Process
			        	{
			        		StartInfo = new ProcessStartInfo
			        		            	{
												FileName = String.Format("{0}/{1}", this.LMPath, this.ApplicationName),
			        		            		Arguments = this.Arguments,
												WorkingDirectory = this.LMPath
			        		            	}
			        	};

			this.Status = ExecutableStatus.Running;
			
			Log.DebugFormat("Launching: {0} {1}", this.ApplicationName, this.Arguments);

			this._stopwatch.Start();

			p.Start();
			p.WaitForExit();
			
			this.Status = ExecutableStatus.Ready;

			this._stopwatch.Stop();
			Log.InfoFormat("Finished: {0} ms. ({1} {2})", this._stopwatch.Elapsed, this.ApplicationName, this.Arguments);
			this._stopwatch.Reset();
		}
	}
}
