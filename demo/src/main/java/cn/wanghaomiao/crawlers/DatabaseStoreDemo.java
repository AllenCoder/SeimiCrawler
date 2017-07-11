package cn.wanghaomiao.crawlers;

import cn.wanghaomiao.dao.StoreToDbDAO;
import cn.wanghaomiao.model.BlogContent;
import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.xpath.model.JXDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashSet;
import java.util.List;

import static cn.wanghaomiao.crawlers.StoreInFile.getAbsoluteURL;

/**
 * 将解析出来的数据直接存储到数据库中
 *
 * @author 汪浩淼 [et.tw@163.com]
 * @since 2015/10/21.
 */
@Crawler(name = "storedb")
public class DatabaseStoreDemo extends BaseSeimiCrawler {
    @Autowired
    private StoreToDbDAO storeToDbDAO;
    private HashSet<String> crawlerUrl = new HashSet<>();

    @Override
    public String[] startUrls() {
        return new String[]{"http://www.cnblogs.com/"};
    }

    @Override
    public void start(Response response) {
        JXDocument doc = response.document();
        try {
            List<Object> urls = doc.sel("//@href");
            logger.info("{}", urls.size());
            for (Object s : urls) {
                if (s.toString().startsWith("http")) {
                    logger.info("url", s.toString());
                    crawlerUrl.add(s.toString());
                    String url = s.toString();
                    if (!url.startsWith("http")) {
                        url = getAbsoluteURL(response.getRealUrl(), url.toString());
                    }
                    push(new Request(url, "renderBean"));
                }
            }
//            List<Object> imgs = doc.sel("//img/@src");
//            for (Object u : imgs) {
//
//                String url = u.toString();
//                if(!url.startsWith("http")){
//                    url =getAbsoluteURL(response.getRealUrl(),url.toString());
//                }
//                push(new Request( url, "renderBean"));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderBean(Response response) {
        try {
            BlogContent blog = response.render(BlogContent.class);
            logger.info("bean resolve res={},url={}", blog, response.getUrl());
            //使用神器paoding-jade存储到DB
            int blogId = storeToDbDAO.save(blog);
            logger.info("store sus,blogId = {}", blogId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void callByCron() {
        logger.info("我是一个根据cron表达式执行的调度器，5秒一次");
//        // 可定时发送一个Request
        if (crawlerUrl.size() > 0) {
            for (String url : crawlerUrl
                    ) {
                push(Request.build(url, "start").setSkipDuplicateFilter(true));
            }
            crawlerUrl.clear();
        } else {
            push(Request.build(startUrls()[0], "start").setSkipDuplicateFilter(true));
        }
//        push(Request.build(startUrls()[0], "start").setSkipDuplicateFilter(true));

    }
}
