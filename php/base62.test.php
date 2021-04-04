<?php require_once 'base62.php' ?>
<!DOCTYPE html>
<html>
<body>
<code>
<?php
function toHexa(string $str) {
	$bin = unpack('C*', $str) ;
	$len = count($bin) ;
	$ret = "" ;
	for($i = 1;$i <= $len; ++$i) {
		$ret .= sprintf("%02X ", $bin[$i]) ;
		if($i % 16 == 0)
			$ret .= "\n" ;
		else if($i % 16 == 8)
			$ret .= " " ;
	}
	return $ret ;
}

try {
	$bin = "" ;
	for($i = 0; $i < 256; ++$i)
		$bin .= chr($i) ;
	printf("----\$bin[%d]----<br/>", strlen($bin)) ;
	printf("%s<br/>", nl2br(toHexa($bin))) ;
	$txt = Base62::encode($bin) ;
	printf("txt:%s<br/>", nl2br($txt)) ;
	$out = Base62::decode($txt) ;
	printf("----\$out[%d]----<br/>", strlen($out)) ;
	printf("%s<br/>", nl2br(toHexa($out))) ;
	
} catch(Exception $ex) {
	printf("Exception : %s<br/>", $ex->getMessage()) ;
}
?>
</code>
</body>
</html>