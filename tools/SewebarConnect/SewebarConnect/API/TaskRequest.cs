﻿using System;
using System.IO;
using System.Text.RegularExpressions;
using System.Xml;
using System.Xml.XPath;
using SewebarConnect.API;
using SewebarConnect.Controllers;

namespace SewebarWeb.API
{
	public class TaskRequest : Request
	{
		private static readonly string InvalidChars = String.Format(@"[{0}]+", Regex.Escape(new string(Path.GetInvalidFileNameChars())));

		private string _taskName;
		private string _taskFileName;
		private string _taskPath;

		public string Task
		{
			get
			{
				return this.HttpContext.Request["content"];
			}
		}

		public string TaskName
		{
			get
			{
				if (this._taskName == null)
				{
					using (var stream = new StringReader(this.Task))
					{
						var xpath = new XPathDocument(stream);
						var docNav = xpath.CreateNavigator();

						if (docNav.NameTable != null)
						{
							var nsmgr = new XmlNamespaceManager(docNav.NameTable);
							nsmgr.AddNamespace("guha", "http://keg.vse.cz/ns/GUHA0.1rev1");
							nsmgr.AddNamespace("pmml", "http://www.dmg.org/PMML-4_0");

							var node = docNav.SelectSingleNode("/pmml:PMML/*/@modelName", nsmgr);
							_taskName = node != null ? node.Value : null;
						}
					}
				}

				return _taskName ?? "task";
			}
		}

		public string TaskFileName
		{
			get
			{
				if (String.IsNullOrEmpty(this._taskFileName))
				{
					this._taskFileName = Regex.Replace(this.TaskName, InvalidChars, "_");
				}

				return _taskFileName;
			}
		}

		public string TaskPath
		{
			get
			{
				if (String.IsNullOrEmpty(this._taskPath))
				{
					this._taskPath = String.Format("{0}/task_{1}_{2:yyyyMMdd-Hmmss}.xml",
												   this.DataFolder,
												   this.TaskFileName,
												   DateTime.Now);
				}

				if(!File.Exists(this._taskPath))
				{
					// save importing task XML
					using (var file = new StreamWriter(this._taskPath))
					{
						file.Write(this.Task);
					}
				}

				return this._taskPath;
			}
		}

		public TaskRequest(BaseController controller)
			: base(controller.Miner, controller.HttpContext)
		{
		}
	}
}