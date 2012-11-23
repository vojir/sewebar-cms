<?php 

  defined('_JEXEC') or die('Restricted access');
           
  echo '<h1>'.JText::_('NEW_DABATASE_CONNECTION').'</h1>';
  echo '<h2>'.JText::_('DATABASE_SETTINGS').'</h2>';
  echo '<form method="post" id="select_table_form">
          <table class="myPlainTable">
            <tr>
              <td>'.JText::_('DB_TYPE').'</td>
              <td><input type="text" name="db_type" value="'.JRequest::getVar('db_type','').'" readonly="readonly" class="noborder" /></td>
            </tr>
            <tr>
              <td>'.JText::_('DB_SERVER').'</td>
              <td><input type="text" name="db_server" value="'.JRequest::getVar('db_server','').'" readonly="readonly" class="noborder" /></td>
            </tr>
            <tr>
              <td>'.JText::_('USERNAME').'</td>
              <td><input type="text" name="db_username" value="'.JRequest::getVar('db_username','').'" readonly="readonly" class="noborder" /></td>
            </tr>
            <tr>
              <td>'.JText::_('PASSWORD').'</td>
              <td><input type="password" name="db_password" value="'.JRequest::getVar('db_password','').'" readonly="readonly" class="noborder" /></td>
            </tr>
            <tr>
              <td>'.JText::_('DATABASE_NAME').'</td>
              <td><input type="text" name="db_database" value="'.JRequest::getVar('db_database','').'" readonly="readonly" class="noborder" /></td>
            </tr>
            <tr>
              <td>'.JText::_('SHARED_CONNECTION').'</td>
              <td><input type="checkbox" name="db_shared_connection" readonly="readonly" disabled="disabled" value="1" '.(JRequest::getVar('db_shared_connection','')==1?'checked="checked"':'').' /></td>
            </tr>
          </table>';
  echo '  <h2>'.JText::_('SELECT_TABLE').'</h2>';  
  if (($this->tables)&&count($this->tables>0)){
    echo '<table class="myAdminTable">';
    echo '<tr><th></th><th>'.JText::_('TABLE_NAME').'</th><th>'.JText::_('ROWS_COUNT').'</th><th>'.JText::_('ACTIONS').'</th></tr>';
    foreach ($this->tables as $table) {
      echo '<tr>
              <td><input type="radio" name="db_table" id="db_table_'.$table['Name'].'" value="'.$table['Name'].'" class="tableRadio" /></td>
              <td><strong>'.$table['Name'].'</strong></td>
              <td>'.$table['Rows'].'</td>
              <td class="actionsTd">
                <a href="javascript:selectTable(\''.$table['Name'].'\');">'.JText::_('SELECT_THIS_TABLE').'</a>
                <a href="'.JRoute::_('index.php?option=com_dbconnect&task=showTable&tmpl=component&db_type='.JRequest::getVar('db_type','mysql').'&db_server='.JRequest::getVar('db_server','localhost').'&db_username='.JRequest::getVar('db_username','').'&db_password='.urlencode(base64_encode(JRequest::getVar('db_password',''))).'&db_database='.JRequest::getVar('db_database').'&db_table='.$table['Name'].'&connection_id='.$connection->id).'" class="modal">'.JText::_('SHOW_DB_TABLE_PREVIEW').'</a>
              </td>
            </tr>';	
    }
    echo '</table>';
  }else{
    echo 'Nepodařilo se načíst data o žádných tabulkách...';
    echo '<a href="parent.history.go(-1);" class="back">&lt;&nbsp;'.JText::_('BACK').'</a>'; 
  }
  echo '<input type="hidden" name="quickDMTask" value="'.@$this->quickDMTask.'">';
  echo '<input type="hidden" name="step" value="2" />';
  echo '<div class="actionsDiv"><input type="submit" value="'.JText::_('CONTINUE').'" class="button disabled" disabled="disabled" id="dbSubmitButton" /></div>';
  echo '</form>';
  

?> 

