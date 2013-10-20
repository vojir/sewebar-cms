﻿using System;
using Iesi.Collections.Generic;

namespace SewebarKey
{
	public class User
	{
		private ISet<Database> _databases;
		private ISet<Miner> _miners;
		private string _role;

		public virtual Guid Id { get; set; }

		public virtual string Username { get; set; }

		public virtual string Password { get; set; }

		public virtual string Email { get; set; }

		public virtual string Role
		{
			get { return _role ?? "user"; }
			set { _role = value; }
		}

		public virtual ISet<Database> Databases
		{
			get { return _databases ?? (_databases = new HashedSet<Database>()); }
			set { _databases = value; }
		}

		public virtual ISet<Miner> Miners
		{
			get { return _miners ?? (_miners = new HashedSet<Miner>()); }
			set { _miners = value; }
		}

		public virtual bool IsAdmin
		{
			get
			{
				return this.Role.ToLowerInvariant() == "admin";
			}
		}
	}
}