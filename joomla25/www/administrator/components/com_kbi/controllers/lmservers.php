<?php
/**
 * @version		$Id$
 * @package		com_kbi
 * @author		Andrej Hazucha
 * @copyright	Copyright (C) 2010 All rights reserved.
 * @license		GNU/GPL, see LICENSE.php
 */

// no direct access
defined('_JEXEC') or die('Restricted access');

jimport( 'joomla.application.component.controller' );

/**
 * Controller for sources administration.
 *
 * @package		com_kbi
 */
class KbiControllerLmservers extends JController
{
	private $com_kbi = 'com_kbi';

	/**
	 * Constructor
	 */
	function __construct( $config = array() )
	{
		parent::__construct( $config );

		// Register Extra tasks
		$this->registerTask('add', 'edit');
		$this->registerTask('apply','save');
		$this->registerTask('register', 'register');
	}

	function display()
	{
		KbiHelpers::addSubmenu('lmservers');

		$document = JFactory::getDocument();
		$view = $this->getView('lmservers', $document->getType());
		$model = $this->getModel('lmservers');

		$user	=& JFactory::getUser();
		//$limit		= $mainframe->getUserStateFromRequest( 'global.list.limit',		'limit',		$mainframe->getCfg('list_limit'), 'int' );
		//$limitstart	= $mainframe->getUserStateFromRequest( $context.'limitstart',	'limitstart',	0, 'int' );

		//$orderby = ' ORDER BY '. $filter_order .' '. $filter_order_Dir .', id';

		$rows = $model->getList($total, 0, 100);
		$lists = array();

		jimport('joomla.html.pagination');
		$pageNav = new JPagination( $total, 0, 100);

		// table ordering
		//$lists['order_Dir']	= $filter_order_Dir;
		//$lists['order']		= $filter_order;

		$view->setLayout('default');

		$view->assignRef('rows', $rows);
		$view->assignRef('pageNav', $pageNav);
		$view->assignRef('lists', $lists);
		$view->display();
	}

	function edit()
	{
		$document = JFactory::getDocument();
		$view = $this->getView('lmserver', $document->getType());

		// Get/Create the model
		if ($model = &$this->getModel('lmservers')) {
			// Push the model into the view (as default)
			$view->setModel($model, true);
		}

		$view->setLayout('default');
		$view->display();
	}

	function save()
	{
		// Check for request forgeries
		JRequest::checkToken() or jexit('Invalid Token');

		$this->setRedirect("index.php?option={$this->com_kbi}&controller=lmservers");

		// Initialize variables
		$table	= JTable::getInstance('lmserver', 'Table');

		if (!$table->save( JRequest::get( 'post' ) )) {
			return JError::raiseWarning( 500, $table->getError() );
		}

		switch (JRequest::getCmd( 'task' ))
		{
			case 'apply':
				$this->setRedirect("index.php?option={$this->com_kbi}&task=edit&id[]={$table->id}&controller=lmservers");
				break;
		}

		$this->setMessage( JText::_( 'Item Saved' ) );
	}

	function remove()
	{
		// Check for request forgeries
		JRequest::checkToken() or jexit( 'Invalid Token' );

		$this->setRedirect("index.php?option={$this->com_kbi}&controller=lmservers");

		// Initialize variables
		$ids	= JRequest::getVar( 'cid', array(0), 'post', 'array' );
		$table	=& JTable::getInstance('lmserver', 'Table');
		$n		= count( $ids );

		for ($i = 0; $i < $n; $i++)
		{
			if (!$table->delete( (int) $ids[$i] ))
			{
				return JError::raiseWarning( 500, $table->getError() );
			}
		}

		$this->setMessage( JText::sprintf( 'Items removed', $n ) );
	}

	function cancel()
	{
		// Check for request forgeries
		JRequest::checkToken() or jexit( 'Invalid Token' );

		$this->setRedirect("index.php?option={$this->com_kbi}&controller=lmservers");
	}
	
	function register()
	{
		// Check for request forgeries
		JRequest::checkToken() or jexit( 'Invalid Token' );
		
		$id	= JRequest::getVar( 'id', array(0), 'post', 'array' );
		
		$this->setRedirect("index.php?option={$this->com_kbi}&id[]={$id[0]}&controller=registerlmserver");
	}
}
