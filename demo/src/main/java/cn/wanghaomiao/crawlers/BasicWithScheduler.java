package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.dao.mybatis.MybatisStoreDAO;
import cn.wanghaomiao.model.BlogContent;
import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "basicWithScheduler")
public class BasicWithScheduler extends BaseSeimiCrawler {
    @Autowired
    private MybatisStoreDAO storeToDbDAO;
    private HashSet<String> url= new HashSet<>();
    @Override
    public String[] startUrls() {
        return new String[]{"http://blog.csdn.net/u010350809"};
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//a/@href");
            logger.info("{}", urls.size());
            for (Object s:urls){
                if (s.toString().startsWith("http")){
                    logger.info("url",s.toString());
                    url.add(s.toString());
                    push(new Request(s.toString(),"renderBean"));

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void renderBean(Response response) {
        try {
            BlogContent blog = response.render(BlogContent.class);
            logger.info("bean resolve res={},url={}", blog, response.getUrl());
            //使用神器paoding-jade存储到DB
            int changeNum = storeToDbDAO.save(blog);
            int blogId = blog.getId();
            logger.info("store success,blogId = {},changeNum={}", blogId, changeNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getTitle(Response response){
        JXDocument doc = response.document();
        try {
            logger.info("url:{} {}", response.getUrl(), doc.sel("//h1[@class='postTitle']/a/text()|//a[@id='cb_post_title_url']/text()"));
            //do something
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Scheduled(fixedDelay = 1000)
//    public void callByFixedTime(){
//        logger.info("我是一个固定间隔调度器,1秒一次");
//    }

//    @Scheduled(cron = "0/5 * * * * ?")
    public void callByCron(){
        logger.info("我是一个根据cron表达式执行的调度器，5秒一次");
//        // 可定时发送一个Request
//        if (url.size()>0){
//            for (String url:url
//                 ) {
//                push(Request.build(url,"start").setSkipDuplicateFilter(true));
//            }
//           url.clear();
//        }else {
//            push(Request.build(startUrls()[0],"start").setSkipDuplicateFilter(true));
//        }
        push(Request.build(startUrls()[0],"start").setSkipDuplicateFilter(true));

    }
}
