<!DOCTYPE html>
<!--
To change this license header, choose License Headers in Project Properties.
To change this template file, choose Tools | Templates
and open the template in the editor.
-->
<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>
    <body>
        <?php
//            $html = file_get_contents('https://www.dcard.tw/f/sex');// https://www.dcard.tw/f/sex?latest=true
//            $dom = new DOMDocument();
//            @$dom->loadHTML($html);
//            // grab all the on the page
//            $xpath = new DOMXPath($dom);
//            $img = $xpath->evaluate("/html/body//div[@class='PostEntry_image_1E6Mi']");
////            $hrefs = $xpath->evaluate("/html/body//a[@class='PostEntry_root_V6g0r']");
//            for ($i = 0; $i < $img->length; $i++) {
////                $arr = array(1,2,3,4,5);
////                $arr -> item(1)
//                $href = $img->item($i);
//                $artHref = $href->parentNode->parentNode->parentNode->getAttribute('href');
//                $style = $href->getAttribute('style');
//                $imgUrl = substr($style, 21, 35);
//                echo '<a target="_blank" href="https://www.dcard.tw/'.$artHref.'"><img src="'.$imgUrl.'" width="20%"></a>';
//                
//            }
            $html = file_get_contents('http://www.bbc.co.uk/learningenglish/chinese/features/6-minute-english');// https://www.dcard.tw/f/sex?latest=true
//            echo $html;
            $dom = new DOMDocument();
            @$dom->loadHTML($html);
            // grab all the on the page
            $xpath = new DOMXPath($dom);
            $img = $xpath->evaluate("/html/body//img[@id='1_p05hggpr']");///div[id='bbcle-content']
//            $hrefs = $xpath->evaluate("/html/body//a[@class='PostEntry_root_V6g0r']");
//            for ($i = 0; $i < $img->length; $i++) {
//                $arr = array(1,2,3,4,5);
//                $arr -> item(1)
//            echo $img->length;
                $href = $img->item(0);
//                print_r($href);
                $artHref = $href->parentNode->getAttribute('href');
//                $style = $href->getAttribute('style');
//                $imgUrl = substr($style, 21, 35);
//                echo "http://www.bbc.co.uk/".$artHref;
//                echo '<a target="_blank" href="https://www.dcard.tw/'.$artHref.'"><img src="'.$imgUrl.'" width="20%"></a>';
                
//            }
                
                $html = file_get_contents("http://www.bbc.co.uk".$artHref);
                $dom = new DOMDocument();
                @$dom->loadHTML($html);
                // grab all the on the page
                $xpath = new DOMXPath($dom);
                $img = $xpath->evaluate("/html/body//a[.='bbcle-download-extension-mp3']");
                $href = $img->item(0);
//                print_r($href);
                $artHref = $href->getAttribute('href');
//                $style = $href->getAttribute('style');
//                $imgUrl = substr($style, 21, 35);
                echo $artHref;//"http://www.bbc.co.uk/".
                copy($artHref,"C:\\Xampp\\123.mp3");
        ?>
    </body>
</html>
