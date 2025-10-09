package com.jfeat.module.io.json.services.domain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jfeat.module.io.json.services.domain.service.MockJsonService;
import com.jfeat.am.core.jwt.JWTKit;
import com.jfeat.module.frontPage.services.domain.service.FrontPageModuleInfoService;
import com.jfeat.module.frontPage.services.gen.persistence.dao.FrontPageMapper;
import com.jfeat.module.frontPage.services.gen.persistence.model.FrontPage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author admin
 * @since 2017-10-16
 */

@Service("mockJsonService")
public class MockJsonServiceImpl implements MockJsonService {
    /**
     * 返回所有基于 appid 的配置列表
     * app.map [  {"appid": "", "appkey": ""}  ]
     * 提供API 设置当前 appid,  api由appkey授权,  一个app一个保存目录, 每个目录一个 site.map，  {"id": "2323", "filename":"个人中心.json"}
     */
//    private static final String JSON_MOCK_DIR = "pages";

//    private static String dir = JSON_MOCK_DIR;
    // private static String mockMapPath = "mock/mock.properties";

//    @Resource
//    MockJsonService mockJsonService;

    @Resource
    FrontPageMapper frontPageMapper;

    @Resource
    FrontPageModuleInfoService frontPageModuleInfoService;

    /**
     * Mock映射文件路径
     */
    // private static String mockMappingFile = "mock/mock.properties";

  

    @Override
    public JSONObject readJsonFile(String name){
       return readJsonFile(name, null);
    }

    @Override
    public JSONObject readJsonFile(String name, String tag) {
        // checkAppMap();

        QueryWrapper<FrontPage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(FrontPage.PAGE_ID, name);
        FrontPage frontPage = frontPageMapper.selectOne(queryWrapper);
        String content = frontPage.getContent();
        JSONObject json = JSONObject.parseObject(content);

//        //如果有tag，则用tag查出来的id作为
//        if(tag != null && !tag.equals("")){
//            QueryWrapper<FrontPage> qw = new QueryWrapper<>();
//            qw.eq("tag",tag);
//            FrontPage frontPage = frontPageMapper.selectOne(qw);
//            if(frontPage == null){throw new BusinessException(BusinessCode.BadRequest,"tag："+tag+" 对应的页面配置不存在");}
//            name  =  frontPage.getPageId();
//        }

//        JSONObject json = new JSONObject();
//
//        // Map<String, String> idMap = getIdMap();
//        // String fileName = idMap.get(id.toString());
//        String fileName = getMockFileName(name);
//        if (fileName == null || fileName.equals("")) {
//            throw new BusinessException(BusinessCode.BadRequest, "该id对应的数据不存在");
//        } else {
//
//            File jsonFile = new File(dir + File.separator + fileName);
//            FileReader fileReader = null;
//            try {
//                fileReader = new FileReader(jsonFile);
//                Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
//                int ch = 0;
//                StringBuffer sb = new StringBuffer();
//                while ((ch = reader.read()) != -1) {
//                    sb.append((char) ch);
//                }
//                fileReader.close();
//                reader.close();
//
//                json = (JSONObject) JSONObject.parse(sb.toString());
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
        return json;
    }

    @Override
    public Integer saveJsonToFile(JSONObject json, String name) {
       return saveJsonToFile(json, name,null);
    }

    @Override
    public Integer saveJsonToFile(JSONObject json, String name, String tag) {
        // checkAppMap();

        Integer i = 0;
        // Map<String, String> idMap = getIdMap();
        //已有id处理
        // String savefileName = idMap.get(id.toString());
//        String fileName = getMockFileName(name);
        // String fileName;
        // if (savefileName != null && !"".equals(savefileName)) {
        //     fileName = savefileName;
        // } else {
        //     fileName = IdWorker.getIdStr() + ".json";
        // }


        //写入数据库
        i+= saveJsonToDataBase(json,name,tag);

        // 无需写入文件
        // @when 2025-09

//        String content = JSON.toJSONString(json, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteDateUseDateFormat);
//
//        File file = new File(dir + File.separator + fileName);
//        try {
//            if (file.exists()) {
//                file.delete();
//            }
//            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
//            write.write(content);
//            write.flush();
//            write.close();
//
//            // FileUtil.writeProperties(id.toString(), fileName, FileUtil.getFile(dir + File.separator
//            //         , dir + File.separator + "appSite.properties"));
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return i;
    }


//    private String getMockFileName(String name){
//        if(name.endsWith(".json")){
//            return name;
//        }
//        return name.concat(".json");
//    }

    // //检查appId是否已记录进配置文件
    // void checkAppMap() {
    //     Map<String, String> appIdMap = getAppIdMap();
    //     if (appIdMap.get(appId) == null) {
    //         FileUtil.writeProperties(appId, appId, FileUtil.getFile(dir, dir + File.separator + "appMap.properties"));
    //     }
    // }

    // /**
    //  * 获取文件中的内容,并返回map
    //  *
    //  * @return
    //  */
    // @Override
    // public Map<String, String> getIdMap() {
    //     return FileUtil.readProperties(dir + File.separator + appId, dir + File.separator + appId + File.separator + "appSite.properties");
    // }

    @Override
    public Integer saveJsonToDataBase(JSONObject json, String pageId, String tag) {

        Integer affect = 0;

//        String jsonFileName = idMap.get(String.valueOf(id));
//        String jsonPath = String.join(JSON_MOCK_DIR, File.separator, name, ".json");
        String title = "";
        if (json!=null&&json.get("title")!=null){
            title = json.get("title").toString();
        }

        // 增加用户id
        String userAccount = JWTKit.getAccount();
        Long userId = JWTKit.getUserId();

        JSONObject userInfo = new JSONObject();
        userInfo.put("account", userAccount);
        userInfo.put("id", userId);
//        userInfo.put("orgId", userOrgId);
        json.put("userInfo:", userInfo);

        FrontPage record = new FrontPage();
        record.setPageId(pageId);
        record.setUserId(userId);
        record.setTitle(title);
        record.setContent(json.toJSONString());
        // record.setJsonName(pageId);
//        record.setJsonPath(jsonPath);
        record.setTag(tag);

//        更新type和moduleName
        frontPageModuleInfoService.setTypeAndModuleName(record,json);

        QueryWrapper<FrontPage> pageQueryWrapper = new QueryWrapper<>();
        pageQueryWrapper.eq(FrontPage.PAGE_ID, pageId);
        FrontPage frontPage = frontPageMapper.selectOne(pageQueryWrapper);
        if (frontPage==null){
            // record.setAppid(mockJsonService.getAppId());
            affect+=frontPageMapper.insert(record);
        }else {
            record.setId(frontPage.getId());
            affect+=frontPageMapper.updateById(record);
        }
        return affect;
    }


    // @Override
    // @Transactional
    // public Integer synchronizationToDataBase() {
    //     // File jsonMockDirMapFile = new File(mockMapPath);
    //     int affect = 0;

    //     // Map<String,String> jsonMockDirMap = FileUtil.readProperties(jsonMockDirMapFile);
    //     // Iterator<Map.Entry<String, String>> iterator = jsonMockDirMap.entrySet().iterator();

    //     // String appid = mockJsonService.getAppId();

    //     while (iterator.hasNext()){
    //         Map.Entry<String, String> entry = iterator.next();
    //         // mockJsonService.setAppId(entry.getKey());
    //         // Map<String, String> idMap = mockJsonService.getIdMap();

    //         // String currentAppid = mockJsonService.getAppId();

    //         for (Map.Entry<String,String> idMapEntry : idMap.entrySet()){
    //             String jsonPath = "jsonMock"+File.separator+entry.getValue()+File.separator+idMapEntry.getValue();
    //             JSONObject contentJson =  mockJsonService.readJsonFile(Long.parseLong(idMapEntry.getKey()));
    //             String title = "";
    //             if (contentJson!=null && contentJson.get("title")!=null){
    //                 title = contentJson.get("title").toString();
    //             }

    //             FrontPage record = new FrontPage();
    //             record.setPageId(idMapEntry.getKey());
    //             record.setTitle(title);
    //             record.setContent(contentJson.toJSONString());
    //             // record.setAppid(currentAppid);
    //             record.setJsonName(idMapEntry.getValue());
    //             record.setJsonPath(jsonPath);

    //             QueryWrapper<FrontPage> pageQueryWrapper = new QueryWrapper<>();
    //             // pageQueryWrapper.eq(FrontPage.APPID, currentAppid).eq(FrontPage.JSON_NAME,idMapEntry.getValue());
    //             FrontPage frontPage = frontPageMapper.selectOne(pageQueryWrapper);
    //             if (frontPage==null){
    //                 affect+=frontPageMapper.insert(record);
    //             }else {
    //                 record.setId(frontPage.getId());
    //                 affect+=frontPageMapper.updateById(record);
    //             }
    //         }

    //     }
    //     // mockJsonService.setAppId(appid);

    //     return affect;
    // }

}
