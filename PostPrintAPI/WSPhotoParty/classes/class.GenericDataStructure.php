<?php
	class GenericDataStructure
	{
		private $data;

		public function __construct( $data )
		{
			if ( !is_array( $data ) )
				$data = array($data);
			$this->data = $data;
		}
		public function toSerializePHP()
		{
			return serialize( $this->data );
		}
		public function toPHP()
		{
			return var_export( $this->data, true );
		}
		public function toJSON()
		{
			return json_encode( $this->data );
			return $this->formatJSON( json_encode( $this->data ) );
		}
		public function toXML()
		{
			$xml = new SimpleXMLElement('<?xml version="1.0" encoding="UTF-8" ?><return></return>');
			$this->nodeToXML( $this->data, $xml );
			return $xml->asXML();
		}
		private function nodeToXML( $arr, $xml, $tag = 'response' )
		{
			foreach( $arr as $k => $v )
			{
				if( is_array( $v ) )
				{
					if ( array_key_exists( 0, $v ) )
						$this->nodeToXML( $v, $xml, $k );
					else
					{
						if ( is_numeric( $tag ) && !$tag )
							$tag = 'undefined';
						$k = is_numeric( $k ) ? $tag : $k ;
						$container = $xml->addChild($k);
						$this->nodeToXML( $v, $container, $k );
					}
				}
				else
				{
					$k = is_numeric( $k ) ? $tag : $k ;
					$xml->addChild($k, $v);
				}
			}
		}
		private function formatJSON( $json )
		{
			$tab = "  ";
			$new_json = "";
			$indent_level = 0;
			$in_string = false;

			$json_obj = json_decode($json);

			if($json_obj === false)
				return false;

			$json = json_encode($json_obj);
			$len = strlen($json);

			for($c = 0; $c < $len; $c++)
			{
				$char = $json[$c];
				switch($char)
				{
					case '{':
					case '[':
						if(!$in_string)
						{
							$new_json .= $char . "\n" . str_repeat($tab, $indent_level+1);
							$indent_level++;
						}
						else
						{
							$new_json .= $char;
						}
						break;
					case '}':
					case ']':
						if(!$in_string)
						{
							$indent_level--;
							$new_json .= "\n" . str_repeat($tab, $indent_level) . $char;
						}
						else
						{
							$new_json .= $char;
						}
						break;
					case ',':
						if(!$in_string)
						{
							$new_json .= ",\n" . str_repeat($tab, $indent_level);
						}
						else
						{
							$new_json .= $char;
						}
						break;
					case ':':
						if(!$in_string)
						{
							$new_json .= ": ";
						}
						else
						{
							$new_json .= $char;
						}
						break;
					case '"':
						if($c > 0 && $json[$c-1] != '\\')
						{
							$in_string = !$in_string;
						}
					default:
						$new_json .= $char;
						break;                   
				}
			}

			return $new_json;
		}
	}
