//from: components/com_kbi/assets/js.js

function KbiPostArb(id,src_type,query_type)
{
  var params = document.getElementById('arb'+id).innerHTML;
  alert('Pravidlo '+id+': ajax');
	var myAjax = new Ajax('http://sewebar-dev.vse.cz/index.php?option=com_kbi&amp;controller=server&amp;format=raw',
		{
			method: 'post',
			update: $('arb_result'+id), //id element, do ktereho prijde vysledek
			data:
			{
				source: src_type, //typ_zdroje (Lucene, Ontopia..) 
				query: query_type, //typ_dotazu (vyjimka, podobnost)
				params: params, //arBuilder = vygenerovane XML
				xslt: null //nic
			}
		}).request();	
	return false;
}

