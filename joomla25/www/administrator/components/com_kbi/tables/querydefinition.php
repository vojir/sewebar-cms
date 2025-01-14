<?php
/**
 * @version		$Id$
 * @package		com_kbi
 * @author		Andrej Hazucha
 * @copyright	Copyright (C) 2010 All rights reserved.
 * @license		GNU/GPL, see LICENSE.php
 */

// no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );

/**
 * Mapping to sources database table.
 *
 * @package com_kbi
 */
class TableQuerydefinition extends JTable
{
	/** @var int */
	var $id = null;
	/** @var string */
	var $name = '';
	/** @var string */
	var $source = '';
	/** @var string */
	var $query = '';
	/** @var string */
	var $xslt;
	/** @var string */
	var $icon;

	function __construct( &$_db )
	{
		parent::__construct( '#__kbi_querydefinitions', 'id', $_db );
	}
}
?>
