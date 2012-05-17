<?php
/**
 * @version		$Id$
 * @package		KBI
 * @author		Andrej Hazucha
 * @copyright	Copyright (C) 2010 All rights reserved.
 * @license		GNU/GPL, see LICENSE.php
 */

require_once dirname(__FILE__).'/../KBIntegrator.php';
require_once dirname(__FILE__).'/../IHasDataDictionary.php';

/**
 * IKBIntegrator implementation for LISp-Miner via SEWEBAR Connect web interface.
 *
 * @package KBI
 */
class LispMiner extends KBIntegrator implements IHasDataDictionary
{
	public function getMethod()
	{
		return isset($this->config['method']) ? $this->config['method'] : 'POST';
	}

	public function getMinerId()
	{
		return isset($this->config['miner_id']) ? $this->config['miner_id'] : NULL;
	}

	public function getPort()
	{
		return isset($this->config['port']) ? $this->config['port'] : 80;
	}
	
	public function __construct($config)
	{
		parent::__construct($config);
	}

	protected function parseRegisterResponse($response)
	{
		$xml_response = simplexml_load_string($response);
		
		if($xml_response['status'] == 'failure') {
			throw new Exception($xml_response->message);
		} else if($xml_response['status'] == 'success') {
			return (string)$xml_response['id'];
		}

		throw new Exception(sprintf('Response not in expected format (%s)', htmlspecialchars($response)));
	}

	protected function parseImportResponse($response)
	{
		$xml_response = simplexml_load_string($response);

		if($xml_response['status'] == 'failure') {
			throw new Exception($xml_response->message);
		} else if($xml_response['status'] == 'success') {
			return (string)$xml_response->message;
		}

		throw new Exception('Response not in expected format');
	}

	public function register($db_cfg)
	{
		$url = trim($this->getUrl(), '/');

		$response = $this->requestPost("$url/Application/Register", $db_cfg);

		KBIDebug::log($response, "Miner registered");
		
		return $this->parseRegisterResponse($response);
	}
	
	public function importDataDictionary($dataDictionary, $server_id = NULL)
	{
		$server_id = $server_id == NULL ? $this->getMinerId() : $server_id;

		if($server_id === NULL) {
			throw new Exception('LISpMiner ID was not provided.');
		}

		$url = trim($this->getUrl(), '/');
		
		$data = array(
			'content' => $dataDictionary,
			'guid' => $server_id
		);
		
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, "$url/DataDictionary/Import");
		curl_setopt($ch, CURLOPT_POSTFIELDS, $this->encodeData($data));
		curl_setopt($ch, CURLOPT_VERBOSE, false);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_POST, true);
		
		$response = curl_exec($ch);
		$info = curl_getinfo($ch);
		curl_close($ch);
		
		KBIDebug::log($response, "Import executed");
		
		return $this->parseRegisterResponse($response);
	}

	public function getDataDescription()
	{
		$url = trim($this->getUrl(), '/');
		$url = "$url/DataDictionary/Export";

		$data = array(
			'guid' => $this->getMinerId(),
			// TODO: make parametrizable
			'matrix' => 'Loans',
			'template' => ''
		);

		KBIDebug::info(array($url, $data));
		$dd = $this->requestCurlPost($url, $data);

		return trim($dd);
	}

	public function queryPost($query, $options)
	{
		if($this->getMinerId() === NULL) {
			throw new Exception('LISpMiner ID was not provided.');
		}

		$url = trim($this->getUrl(), '/');
		$url = "$url/Task/Pool";
		
		$data = array(
			'content' => $query,
			'guid' => $this->getMinerId()
		);

		if(isset($options['template'])) {
			$data['template'] = $options['template'];
			KBIDebug::info("Using LM exporting template {$data['template']}", 'LISpMiner');
		}

		KBIDebug::log(array('URL' => $url, 'POST' => $data), 'LM Query');
		
		$ch = curl_init();
		curl_setopt($ch, CURLOPT_URL, $url);
		
		curl_setopt($ch, CURLOPT_POSTFIELDS, $this->encodeData($data));
		curl_setopt($ch, CURLOPT_VERBOSE, false);
		curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($ch, CURLOPT_POST, true);

		// gain task results from LISpMiner
		$response = curl_exec($ch);
		$info = curl_getinfo($ch);
		curl_close($ch);

		return $response;
	}
}