package com.mininowcoder.community;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mininowcoder.community.dao.DiscussPostMapper;
import com.mininowcoder.community.entity.DiscussPost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by FeiPan on 2022/4/27.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

   @Autowired
   private DiscussPostMapper discussPostMapper;

   @Autowired
   private RestHighLevelClient restHighLevelClient;

   /**
    * 创建索引 创建映射
    */
   @Test
   public void testIndexAndMapping() throws IOException {
      // 参数1-常见索引请求对象  参数2-请求配置对象(默认配置)
      CreateIndexRequest createIndexRequest = new CreateIndexRequest("discusspost");
      createIndexRequest.mapping("{\n" +
              "     \"properties\": {\n" +
              "       \"userId\":{\n" +
              "         \"type\": \"integer\"\n" +
              "       },\n" +
              "       \"title\":{\n" +
              "         \"type\": \"text\"\n" +
              "       },\n" +
              "       \"content\":{\n" +
              "         \"type\": \"text\"\n" +
              "       },\n" +
              "       \"type\":{\n" +
              "         \"type\": \"integer\"\n" +
              "       },\n" +
              "       \"status\":{\n" +
              "         \"type\": \"integer\"\n" +
              "       },\n" +
              "       \"createTime\":{\n" +
              "         \"type\": \"date\"\n" +
              "       },\n" +
              "       \"commentCount\":{\n" +
              "         \"type\":\"integer\"\n" +
              "       },\n" +
              "       \"score\":{\n" +
              "         \"type\": \"double\"\n" +
              "       }\n" +
              "     }\n" +
              "   }", XContentType.JSON);//指定映射 参数1-指定映射json结构 参数2-指定数据类型
      CreateIndexResponse response = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      System.out.println(response.isAcknowledged());
      restHighLevelClient.close();//关闭资源
   }

   /**
    * 删除索引
    */
   @Test
   public void testDeleteIndex() throws IOException {
      // 参数1-删除的索引  参数2-请求配置对象
      AcknowledgedResponse response =
              restHighLevelClient.indices().delete(new DeleteIndexRequest("discusspost"), RequestOptions.DEFAULT);
      System.out.println(response.isAcknowledged());
   }

   /**
    * 索引中插入文档
    */
   @Test
   public void testCreate() throws IOException {
      // 参数1-索引请求对象   参数2-请求配置对象
      IndexRequest indexRequest = new IndexRequest("discusspost");
      DiscussPost discussPost = discussPostMapper.selectDiscussPostById(241);
      String json = JSONObject.toJSONString(discussPost);
      indexRequest.id(String.valueOf(discussPost.getId()))
              .source(json, XContentType.JSON);
      IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
      System.out.println(index.status());
   }

   /**
    * 更新文档
    */
   @Test
   public void testUpdate() throws IOException {
      // 参数1-更新请求对象   参数2-请求配置对象
      DiscussPost discussPost = discussPostMapper.selectDiscussPostById(241);
      discussPost.setContent("测试更新");
      UpdateRequest updateRequest = new UpdateRequest("discusspost", String.valueOf(discussPost.getId()));
      updateRequest.doc(JSONObject.toJSONString(discussPost), XContentType.JSON); // 和重新插入一个效果

      UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
      System.out.println(update.status());
   }

   /**
    * 删除文档
    */
   @Test
   public void testDelete() throws IOException {
      // 参数1-删除请求对象   参数2-请求配置对象
      DeleteResponse delete =
              restHighLevelClient.delete(new DeleteRequest("discusspost", "vZgOb4AB2joIBEPlzwEG"), RequestOptions.DEFAULT);
      System.out.println(delete.status());
   }

   /**
    * 基于id查询文档
    */
   @Test
   public void testQuertById() throws IOException {
      GetRequest getRequest = new GetRequest("discusspost", "241");
      GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
      String json = getResponse.getSourceAsString();
      DiscussPost discussPost = JSONObject.parseObject(json, DiscussPost.class);
      System.out.println(discussPost);
   }

   /**
    * 查询所有
    */
   @Test
   public void testMatchAll() throws IOException {
      // 参数1-搜索的请求对象  参数2-请求配置对象
      SearchRequest searchRequest = new SearchRequest("discusspost");// 指定搜索的索引
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//指定条件对象
      sourceBuilder.query(QueryBuilders.matchAllQuery());// 查询所有
      searchRequest.source(sourceBuilder);//指定查询条件
      SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

      System.out.println(search.getHits().getTotalHits().value);//命中的总条数
      System.out.println(search.getHits().getMaxScore());//最大得分
      SearchHit[] hits = search.getHits().getHits();
      for (SearchHit hit : hits) {
         System.out.println(hit.getSourceAsString());
      }
   }

   /**
    * 不同条件查询
    */
   @Test
   public void testQuery() throws IOException {
      SearchRequest searchRequest = new SearchRequest("discusspost");// 指定搜索的索引
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//指定条件对象

//      sourceBuilder.query(QueryBuilders.termQuery("content", "实习")); // 关键词查找
//      sourceBuilder.query(QueryBuilders.rangeQuery("userId").gt(100).lt(120)); // 范围查找
//      sourceBuilder.query(QueryBuilders.prefixQuery("content","互联")); // 前缀查找?
      sourceBuilder.query(QueryBuilders.multiMatchQuery("实习","title","content")); // 多字段查询

      searchRequest.source(sourceBuilder);//指定查询条件
      SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

      System.out.println(search.getHits().getTotalHits().value);//命中的总条数
      System.out.println(search.getHits().getMaxScore());//最大得分
      SearchHit[] hits = search.getHits().getHits();
      for (SearchHit hit : hits) {
         System.out.println(hit.getSourceAsString());
      }
   }

   /**
    * 查询所有 分页查询 from起始位置 size 每页展示数据量
    * 排序
    * 返回指定字段 _source
    */
   @Test
   public void testSearch() throws IOException {
      SearchRequest searchRequest = new SearchRequest("discusspost");
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      HighlightBuilder highlightBuilder = new HighlightBuilder();
      highlightBuilder.requireFieldMatch(false)
              .field("title")
              .field("content")
              .preTags("<span style='color:red;'>")
              .postTags("</span>");
      searchSourceBuilder.query(QueryBuilders.termQuery("content", "互联网"))
              .from(0)
              .size(2)
              .sort("score", SortOrder.DESC)
              .fetchSource(new String[]{"title", "content"}, new String[]{}) // 参数1-包含字段数组 参数2-排除字段数组
              .highlighter(highlightBuilder);
      searchRequest.source(searchSourceBuilder);//指定查询条件
      SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

      System.out.println(search.getHits().getTotalHits().value);//命中的总条数
      System.out.println(search.getHits().getMaxScore());//最大得分
      SearchHit[] hits = search.getHits().getHits();
      for (SearchHit hit : hits) {
         System.out.println(hit.getSourceAsString());
         // 获取高亮的字段
         Map<String, HighlightField> highlightFields = hit.getHighlightFields();
         if(highlightFields.containsKey("title")){
            System.out.println("title高亮："+highlightFields.get("title").fragments()[0]);
         }
         if(highlightFields.containsKey("content")){
            System.out.println("content高亮："+highlightFields.get("content").fragments()[0]);
         }
      }

   }

   /**
    * query： 精确查询， 查询计算文档得分，并根据文档得分进行返回
    * filter query： 过滤查询，用来在大量数据中筛选处本地查询相关数据  不会计算文档得分  会将经常使用的filter query结果进行缓存
    * 注意：一旦使用query和filter query  elasticsearch会优先执行filter query 然后再执行query
    */
   @Test
   public void testFilterQuery() throws IOException {
      SearchRequest searchRequest = new SearchRequest("discusspost");
      SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
      sourceBuilder.query(QueryBuilders.matchAllQuery())
                      .postFilter(QueryBuilders.termQuery("content","实习"));//指定过滤条件
      searchRequest.source(sourceBuilder);
      SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

      System.out.println(search.getHits().getTotalHits().value);//命中的总条数
      System.out.println(search.getHits().getMaxScore());//最大得分
      SearchHit[] hits = search.getHits().getHits();
      for (SearchHit hit : hits) {
         System.out.println(hit.getSourceAsString());
      }
   }


   /**
    * 以下是该项目中使用的一下小例子
    */
   // 向es中插入帖子
   @Test
   public void insertList() throws IOException {
      IndexRequest indexRequest = new IndexRequest("discusspost");
      int[] userId = new int[]{101, 102, 103, 111, 112, 131, 132, 133, 134};
      for (int id : userId) {
         List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(id, 0, 100, 0);
         if(discussPosts!=null){
            for (DiscussPost post : discussPosts) {
               indexRequest.id(String.valueOf(post.getId()))
                       .source(new ObjectMapper().writeValueAsString(post), XContentType.JSON);
               restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            }
         }
      }
   }

   // 在es中根据一定规则查询帖子，并做高亮显示
   @Test
   public void testSearchList() throws IOException {
      SearchRequest searchRequest = new SearchRequest("discusspost");
      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      HighlightBuilder highlightBuilder = new HighlightBuilder();
      highlightBuilder.requireFieldMatch(false)
              .field("title")
              .field("content")
              .preTags("<span style='color:red;'>")
              .postTags("</span>");
      searchSourceBuilder.query(QueryBuilders.multiMatchQuery("互联网寒冬", "title","content"))
              .sort("type", SortOrder.DESC)
              .sort("score", SortOrder.DESC)
              .sort("createTime", SortOrder.DESC)
              .from(0)
              .size(10)
              .highlighter(highlightBuilder);
      searchRequest.source(searchSourceBuilder);
      SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

      System.out.println(search.getHits().getTotalHits().value);//命中的总条数
      SearchHit[] hits = search.getHits().getHits();
      List<DiscussPost> list = new ArrayList<>();
      for (SearchHit hit : hits) {
         DiscussPost post = new ObjectMapper().readValue(hit.getSourceAsString(), DiscussPost.class);
         // 获取高亮的字段
         Map<String, HighlightField> highlightFields = hit.getHighlightFields();
         if(highlightFields.containsKey("title")){
            post.setTitle(highlightFields.get("title").fragments()[0].toString());
         }
         if(highlightFields.containsKey("content")){
            post.setContent(highlightFields.get("content").fragments()[0].toString());
         }
         list.add(post);
         System.out.println(post);
      }

   }

}






