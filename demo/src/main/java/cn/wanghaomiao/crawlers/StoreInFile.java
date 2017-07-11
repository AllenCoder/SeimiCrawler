package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "savefile")
public class StoreInFile extends BaseSeimiCrawler {

    private HashSet<String> crawlerUrl = new HashSet<>();

    @Override
    public String[] startUrls() {
        return new String[]{"http://www.umei.cc/"};
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//@href");
            logger.info("{}", urls.size());
            for (Object s:urls){
                if (s.toString().startsWith("http")){
                    logger.info("url",s.toString());
                    crawlerUrl.add(s.toString());
//                    push(new Request(s.toString(),"saveFile"));
                }
            }
            List<Object> imgs = doc.sel("//img/@src");
            for (Object u : imgs) {

                String url = u.toString();
                if(!url.startsWith("http")){
                    url =getAbsoluteURL(response.getRealUrl(),url.toString());
                }
                push(new Request( url, "saveFile"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressWarnings("finally")
    public static String getAbsoluteURL(String baseURI, String relativePath){
        String abURL=null;
        try {
            URI base=new URI(baseURI);//基本网页URI
            URI abs=base.resolve(relativePath);//解析于上述网页的相对URL，得到绝对URI
            URL absURL=abs.toURL();//转成URL
            System.out.println(absURL);
            abURL = absURL.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally{
            return abURL;
        }
    }
    public void saveFile(Response response) {
        try {
            String fileName = StringUtils.substringAfterLast(response.getUrl(), "/");
            String path = "F:\\newTest\\cnblog" + fileName;
            response.saveTo(new File(path));
            logger.info("file done = {}", fileName);
        } catch (Exception e) {
            //
            logger.error("file errot = {}", e.getLocalizedMessage());

        }
    }

//    @Scheduled(cron = "0/5 * * * * ?")
//    public void callByCron() {
//        logger.info("我是一个根据cron表达式执行的调度器，5秒一次");
////        // 可定时发送一个Request
////        if (crawlerUrl.size()>0){
////            for (String url:crawlerUrl
////                 ) {
////                push(Request.build(url,"start").setSkipDuplicateFilter(true));
////            }
////            crawlerUrl.clear();
////        }else {
////            push(Request.build(startUrls()[0],"start").setSkipDuplicateFilter(true));
////        }
//        push(Request.build(startUrls()[0], "start").setSkipDuplicateFilter(true));
//
//    }
}
