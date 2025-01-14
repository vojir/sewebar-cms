<?php
/**
 * @version		$Id$
 * @package		KBI
 * @author		Andrej Hazucha
 * @copyright	Copyright (C) 2010 All rights reserved.
 * @license		GNU/GPL, see LICENSE.php
 */

require_once dirname(__FILE__).'/KBIntegrator.php';
require_once dirname(__FILE__).'/ISynchronable.php';

/**
 * Generic implementation for IKBIntegrator.
 *
 * @package KBI
 */
abstract class KBIntegratorSynchronable extends KBIntegrator implements ISynchronable
{
	public function __contruct(Array $config = array())
	{
		$this->config = $config;
	}

	protected function encodeData($array)
	{
		$data = "";
		foreach ($array as $key=>$value) $data .= "{$key}=".urlencode($value).'&';
		return $data;
	}
}
