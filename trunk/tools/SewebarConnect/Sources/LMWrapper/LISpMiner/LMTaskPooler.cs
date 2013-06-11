﻿using System;
using System.Diagnostics;
using System.Text;
using log4net;

namespace LMWrapper.LISpMiner
{
	public class LMTaskPooler : Executable, ITaskLauncher
	{
		private Process _process;

		private readonly Stopwatch _stopwatch;

		protected Process Process
		{
			get
			{
				if (this._process == null)
				{
					this._process = new Process
										{
											EnableRaisingEvents = true,
											StartInfo = new ProcessStartInfo
															{
																FileName = String.Format("{0}/{1}", this.LMPath, this.ApplicationName),
																Arguments = this.Arguments
															},
										};

					this._process.Exited += ProcessExited;
				}

				return this._process;
			}
		}

		/// <summary>
		/// /TaskID [TaskID]		... TaskID of selected task
		/// </summary>
		public string TaskId { get; set; }

		/// <summary>
		/// /TaskName:[TaskName]		... Task.Name of the selected task
		/// </summary>
		public string TaskName { get; set; }

		/// <summary>
		/// /TimeOut [sec]			... optional: time-out in seconds (approx.) after generation (excluding initialisation) is automatically interrupted
		/// </summary>
		public int? TimeOut { get; set; }

		/// <summary>
		/// /ShutdownDelaySec:<n>		... (O) number of seconds <0;86400> before the LM TaskPooler server is shutted down after currently the last waiting task is solved (default: 10)
		/// </summary>
		public int? ShutdownDelaySec { get; set; }

		/// <summary>
		/// /TaskCancel			... (O) to cancel task of given TaskID or name (if already running) or to remove it from queue
		/// </summary>
		public bool TaskCancel { get; set; }

		/// <summary>
		/// /CancelAll			... (O) to cancel any running task and to empty the queue
		/// </summary>
		public bool CancelAll { get; set; }

		public override string Arguments
		{
			get
			{
				var arguments = new StringBuilder("");

				if (!String.IsNullOrEmpty(this.Dsn))
				{
					arguments.AppendFormat("/DSN:{0} ", this.Dsn);
				}

				if (!String.IsNullOrEmpty(this.OdbcConnectionString))
				{
					arguments.AppendFormat("/ODBCConnectionString=\"{0}\" ", this.OdbcConnectionString);
				}

				// /TaskID <TaskID>
				if (!String.IsNullOrEmpty(this.TaskId))
				{
					arguments.AppendFormat("/TaskID:{0} ", this.TaskId);
				}

				// /TaskName <TaskName>
				if (!String.IsNullOrEmpty(this.TaskName))
				{
					arguments.AppendFormat("\"/TaskName:{0}\" ", this.TaskName);
				}

				// /TaskCancel
				if (this.TaskCancel)
				{
					arguments.Append("/TaskCancel ");
				}

				// /CancelAll
				if (this.CancelAll)
				{
					arguments.Append("/CancelAll ");
				}

				// /TimeOut <sec>
				if (this.TimeOut != null)
				{
					arguments.AppendFormat("/TimeOut:{0} ", this.TimeOut);
				}

				// /ShutdownDelaySec:<n>
				if (this.ShutdownDelaySec != null)
				{
					arguments.AppendFormat("/ShutdownDelaySec:{0} ", this.ShutdownDelaySec);
				}

				// /Quiet
				if (this.Quiet)
				{
					arguments.Append("/Quiet ");
				}

				// /NoProgress
				if (this.NoProgress)
				{
					arguments.Append("/NoProgress ");
				}

				// /AppLog
				if (!String.IsNullOrEmpty(this.AppLog))
				{
					arguments.AppendFormat("\"/AppLog:{0}\"", this.AppLog);
				}

				return arguments.ToString().Trim();
			}
		}

		internal LMTaskPooler(LISpMiner lispMiner, ODBC.ConnectionString connectionString, string lmPath)
			: base()
		{
			this.LISpMiner = lispMiner;
			this.LMPath = lmPath ?? this.LISpMiner.LMPath;
			this.OdbcConnectionString = connectionString.Value;

			this.ApplicationName = "LMTaskPooler.exe";
			this._stopwatch = new Stopwatch();
			this.AppLog = String.Format("{0}-{1}.dat", "_AppLog_LMTaskPooler", Guid.NewGuid());
			//this.TimeOut = 10;

			this.CancelAll = false;
		}

		protected override void Run()
		{
			this.Status = ExecutableStatus.Running;

			if (this.CancelAll || this.TaskCancel)
			{
				ExecutableLog.Debug(String.Format("Launching Task cancelation: {0} {1}", this.ApplicationName, this.Arguments));
			}
			else
			{
				ExecutableLog.Debug(String.Format("Launching: {0} {1}", this.ApplicationName, this.Arguments));
			}

			this._stopwatch.Start();

			this.Process.Start();
			// wait a little
			this.Process.WaitForExit(5*1000);
		}

		private void ProcessExited(object sender, EventArgs e)
		{
			// we most probably already exited - there was an issue that this function was called twice
			// using WaitForExit we are mixing synchronous and asynchronous way of process termination notification
			if (this.Status == ExecutableStatus.Ready)
			{
				return;
			}

			this._stopwatch.Stop();
			this.Status = ExecutableStatus.Ready;

			if (this.CancelAll || this.TaskCancel)
			{
				ExecutableLog.DebugFormat("Task cancelation finished in {2} ms: {0} {1}", this.ApplicationName, this.Arguments,
							   this._stopwatch.Elapsed);
			}
			else
			{
				ExecutableLog.DebugFormat("Result generation finished in {2} ms: {0} {1}", this.ApplicationName, this.Arguments,
							   this._stopwatch.Elapsed);
			}

			this.Process.Close();
			this._process = null;
		}
	}
}