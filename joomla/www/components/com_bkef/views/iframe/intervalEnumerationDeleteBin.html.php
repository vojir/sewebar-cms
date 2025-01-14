<?php
/**
 * HTML View class for the gInclude Component
 *  
 * @package    BKEF
 * @license    GNU/GPL
 * @author Stanislav Vojíř - xvojs03
 * @copyright Stanislav Vojíř, 2012
 *   
 */
 
// no direct access
 
defined( '_JEXEC' ) or die( 'Restricted access' );
jimport( 'joomla.application.component.view');
           
class BkefViewIntervalEnumerationDeleteBin extends JView
{
  function display($tpl = null)
  {               
      $doc = & JFactory::getDocument();
      if (JPATH_BASE!=JPATH_ADMINISTRATOR){
        $doc->addStyleSheet('components/com_bkef/css/general.css');
        $doc->addStyleSheet('components/com_bkef/css/component.css');
      }         
      
      echo '<h1>'.JText::_('DELETE_INTERVAL_ENUMERATION_BIN').'</h1>';
      
      $xml=$this->xml;
      $maId=intval($this->maId);
      $fId=intval($this->fId);
      $phId=intval($this->phId);
      $binId=intval($this->binId);
      
      echo '<div>';
      echo JText::_('DELETE_INTERVAL_ENUMERATION_BIN_QUESTION');
      echo '</div>';
      echo '<strong>'.((string)@$this->xml->MetaAttributes[0]->MetaAttribute[$maId]->Formats[0]->Format[$fId]->PreprocessingHints[0]->DiscretizationHint[$phId]->IntervalEnumeration[0]->IntervalBin[$binId]->Name[0]).'</strong>';
      echo '<br /><br />';
      ?>
      <form action="index.php?option=com_bkef&amp;task=intervalEnumerationDeleteBin" method="post" target="_parent" >
        <input type="hidden" name="article" value="<?php echo $this->article; ?>" />
        <input type="hidden" name="maId" value="<?php echo $maId; ?>" />
        <input type="hidden" name="fId" value="<?php echo $fId; ?>" />
        <input type="hidden" name="phId" value="<?php echo $phId; ?>" />
        <input type="hidden" name="potvrzeni" value="1" id="potvrzeni" />
        <input type="submit" value="<?php echo JText::_('DELETE');?>..." />
      </form>
      <?php
      //parent::display($tpl);
  }
}

?>