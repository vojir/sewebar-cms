﻿using System;
using System.Diagnostics;
using System.IO;
using log4net;

namespace LMWrapper.LISpMiner
{
	public abstract class Executable
	{
		protected static readonly ILog ExecutableLog = LogManager.GetLogger(typeof(Executable));

		private readonly Stopwatch _stopwatch;

		private string _appLog = string.Empty;

		public LISpMiner LISpMiner { get; protected set; }

		public string ApplicationName { get; protected set; }

		public ExecutableStatus Status { get; protected set; }

		/// <summary>
		/// /ODBCConnectionString="FILEDSN=X:\Path\File" ... 'File' is DSN file without extension (eg. IZIMiner.MB.dsn)
		/// </summary>
		public string OdbcConnectionString { get; protected set; }

		public string LMExecutablesPath { get; protected set; }

		public string LMPrivatePath { get; protected set; }

		/// <summary>
		/// /Quiet	... errors reported to _AppLog.dat instead on screen
		/// </summary>
		public bool Quiet { get; set; }

		/// <summary>
		/// /NoProgress   ... no progress dialog is displayed
		/// </summary>
		public bool NoProgress { get; set; }

		/// <summary>
		/// /AppLog:[log_file]		... (O) alternative path and file name for logging
		/// </summary>
		public string AppLog {
			get
			{
				return this._appLog;
			}

			protected set
			{
				var errorFilePath = String.IsNullOrEmpty(value)
					? String.Format("{0}/_AppLog.dat", this.LMPrivatePath)
					: String.Format("{0}/{1}", this.LMPrivatePath, value);

				this._appLog = errorFilePath;
			}
		}

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
			this.Run();

			if(File.Exists(this.AppLog))
			{
				var message = File.ReadAllText(this.AppLog);
				File.Delete(this.AppLog);

				throw new LISpMinerException(message);
			}
		}

		protected virtual void Run()
		{
			var info = new ProcessStartInfo
			{
				FileName = String.Format("{0}/{1}", this.LMExecutablesPath, this.ApplicationName),
				Arguments = this.Arguments,
				WorkingDirectory = this.LMExecutablesPath
			};

			using (Process process = Process.Start(info))
			{
				this.Status = ExecutableStatus.Running;
				ExecutableLog.DebugFormat("Launching: {0} {1}", this.ApplicationName, this.Arguments);

				this._stopwatch.Start();
				process.WaitForExit();
				this._stopwatch.Stop();

				this.Status = ExecutableStatus.Ready;
				ExecutableLog.DebugFormat("Finished: {0} ms. ({1} {2})", this._stopwatch.Elapsed, this.ApplicationName, this.Arguments);
			}

			this._stopwatch.Reset();
		}
	}
}
