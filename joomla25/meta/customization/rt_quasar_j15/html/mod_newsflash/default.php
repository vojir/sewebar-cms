<?php
/**
 * @package   Quasar Template - RocketTheme
 * @version   @VERSION@ @BUILD_DATE@
 * @author    RocketTheme http://www.rockettheme.com
 * @copyright Copyright (C) 2007 - @COPYRIGHT_YEAR@ RocketTheme, LLC
 * @license   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPLv2 only
 *
 * Rockettheme Quasar Template uses the Joomla Framework (http://www.joomla.org), a GNU/GPLv2 content management system
 *
 */
// no direct access
defined('_JEXEC') or die('Restricted access'); ?>
<?php
srand((double) microtime() * 1000000);
$flashnum	= rand(0, $items -1);
$item		= $list[$flashnum];
modNewsFlashHelper::renderItem($item, $params, $access);
?>