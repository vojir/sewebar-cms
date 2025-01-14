﻿using System;
using ICSharpCode.SharpZipLib.Zip;

namespace LMWrapper.Utils
{
	public static class ZipUtil
	{
		public static void Unzip(string directory, string zip)
		{
			var package = new FastZip
				              {
					              CreateEmptyDirectories = true
				              };

			package.ExtractZip(zip, directory, String.Empty);
		}
	}
}
