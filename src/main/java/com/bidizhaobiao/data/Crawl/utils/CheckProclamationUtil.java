package com.bidizhaobiao.data.Crawl.utils;

import com.bidizhaobiao.data.Crawl.entity.oracle.PosOfProclamation;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author 作者: 廉建林
 * @version 创建时间：2018年8月10日 下午3:23:06 类说明 根据字符串匹配地址
 */
public class CheckProclamationUtil {
    static String city;
    // 以下对象为地区-省份-地级市的对应关系映射表 ———— Map<地区, Map<省份, 城市数组>>（在本类的静态块中初始化）
    private static Map<String, Map<String, String[]>> areaProvinceCityMap;
    // 以下对象为信息类型与相关关键词对应关系映射表（在本类的静态块中初始化）
    private static Map<String, String[]> infoTypeKeywordMap;
    // 以下对象为行业分类与相关关键词对应关系映射表（在本类的静态块中初始化）
    private static Map<String, String[]> industryKeywordMap;

    // 初始化
    static {
        // 初始化地区-省份-地级市的对应关系映射表 ———— Map<地区, Map<省份, 城市数组>>
        areaProvinceCityMap = new HashMap<String, Map<String, String[]>>();
        areaProvinceCityMap.put("华北", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("北京", new String[]{"北京"});
                put("天津", new String[]{"天津"});
                put("河北", new String[]{"石家庄", "唐山", "秦皇岛", "邯郸", "邢台", "保定", "张家口", "承德", "沧州", "廊坊", "衡水", "定州",
                        "辛集"});
                put("山西", new String[]{"太原", "大同", "阳泉", "长治", "晋城", "朔州", "晋中", "运城", "忻州", "临汾", "吕梁"});
                put("内蒙古", new String[]{"呼和浩特", "包头", "乌海", "赤峰", "通辽", "鄂尔多斯", "呼伦贝尔", "巴彦淖尔", "乌兰察布", "兴安盟",
                        "锡林郭勒盟", "阿拉善盟"});
            }
        });
        areaProvinceCityMap.put("东北", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("黑龙江", new String[]{"哈尔滨", "齐齐哈尔", "鸡西", "鹤岗", "双鸭山", "大庆", "伊春", "佳木斯", "七台河", "牡丹江", "黑河", "绥化",
                        "大兴安岭地区"});
                put("辽宁", new String[]{"沈阳", "大连", "鞍山", "抚顺", "本溪", "丹东", "锦州", "营口", "阜新", "辽阳", "盘锦", "铁岭", "朝阳",
                        "葫芦岛"});
                put("吉林", new String[]{"长春", "吉林市", "四平", "辽源", "通化", "白山", "白城", "松原", "延边朝鲜族自治州", "吉林省长白山保护开发区",
                        "梅河口", "公主岭"});
            }
        });
        areaProvinceCityMap.put("华东", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("上海", new String[]{"上海"});
                put("江苏",
                        new String[]{"南京", "无锡", "徐州", "常州", "苏州", "南通", "连云港", "淮安", "盐城", "扬州", "镇江", "泰州", "宿迁"});
                put("浙江", new String[]{"杭州", "宁波", "温州", "绍兴", "湖州", "嘉兴", "金华", "衢州", "台州", "丽水", "舟山"});
                put("安徽", new String[]{"合肥", "芜湖", "蚌埠", "淮南", "马鞍山", "淮北", "铜陵", "安庆", "黄山", "阜阳", "宿州", "滁州", "六安",
                        "宣城", "池州", "亳州"});
                put("福建", new String[]{"福州", "厦门", "漳州", "泉州", "三明", "莆田", "南平", "龙岩", "宁德"});
                put("江西", new String[]{"南昌", "九江", "上饶", "抚州", "宜春", "吉安", "赣州", "景德镇", "萍乡", "新余", "鹰潭"});
                put("山东", new String[]{"济南", "青岛", "淄博", "枣庄", "东营", "烟台", "潍坊", "济宁", "泰安", "威海", "日照", "滨州", "德州",
                        "聊城", "临沂", "菏泽", "莱芜"});
            }
        });
        areaProvinceCityMap.put("华中", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("河南", new String[]{"郑州", "开封", "洛阳", "平顶山", "安阳", "鹤壁", "新乡", "焦作", "濮阳", "许昌", "漯河", "三门峡", "商丘",
                        "周口", "驻马店", "南阳", "信阳", "济源"});
                put("湖北", new String[]{"武汉", "黄石", "十堰", "宜昌", "襄阳", "鄂州", "荆门", "孝感", "荆州", "黄冈", "咸宁", "随州",
                        "恩施土家族苗族自治州", "仙桃", "潜江", "天门", "神农架林区"});
                put("湖南", new String[]{"长沙", "株洲", "湘潭", "衡阳", "邵阳", "岳阳", "常德", "张家界", "益阳", "娄底", "郴州", "永州", "怀化",
                        "湘西土家族苗族自治州"});
            }
        });
        areaProvinceCityMap.put("华南", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("广东", new String[]{"广州", "深圳", "珠海", "汕头", "佛山", "韶关", "湛江", "肇庆", "江门", "茂名", "惠州", "梅州", "汕尾",
                        "河源", "阳江", "清远", "东莞", "中山", "潮州", "揭阳", "云浮"});
                put("广西", new String[]{"南宁", "柳州", "桂林", "梧州", "北海", "防城港", "钦州", "贵港", "玉林", "百色", "贺州", "河池", "来宾",
                        "崇左"});
                put("海南", new String[]{"海口", "三亚", "五指山", "文昌", "琼海", "万宁", "东方", "定安", "屯昌", "澄迈", "临高", "白沙黎族自治县",
                        "昌江黎族自治县", "乐东黎族自治县", "陵水黎族自治县", "保亭黎族苗族自治县", "琼中黎族苗族自治县", "洋浦经济开发区", "三沙", "儋州"});
            }
        });
        areaProvinceCityMap.put("西南", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("重庆", new String[]{"重庆"});
                put("四川", new String[]{"成都", "绵阳", "自贡", "攀枝花", "泸州", "德阳", "广元", "遂宁", "内江", "乐山", "资阳", "宜宾", "南充",
                        "达州", "雅安", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州", "广安", "巴中", "眉山"});
                put("贵州", new String[]{"贵阳", "六盘水", "遵义", "安顺", "毕节", "铜仁", "黔西南布依族苗族自治州", "黔东南苗族侗族自治州",
                        "黔南布依族苗族自治州"});
                put("云南", new String[]{"昆明", "曲靖", "玉溪", "昭通", "保山", "丽江", "普洱", "临沧", "德宏傣族景颇族自治州", "怒江傈僳族自治州",
                        "迪庆藏族自治州", "大理白族自治州", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州"});
                put("西藏", new String[]{"拉萨", "昌都", "日喀则", "林芝", "山南", "那曲地区", "阿里地区"});
            }
        });
        areaProvinceCityMap.put("西北", new HashMap<String, String[]>() {
            private static final long serialVersionUID = 1L;

            {
                put("陕西", new String[]{"西安", "宝鸡", "咸阳", "渭南", "铜川", "延安", "榆林", "安康", "汉中", "商洛", "杨凌示范区"});
                put("甘肃", new String[]{"兰州", "嘉峪关", "金昌", "白银", "天水", "酒泉", "张掖", "武威", "定西", "陇南", "平凉", "庆阳",
                        "临夏回族自治州", "甘南藏族自治州"});
                put("青海", new String[]{"西宁", "海东", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州",
                        "海西蒙古族藏族自治州"});
                put("宁夏", new String[]{"银川", "石嘴山", "吴忠", "固原", "中卫"});
                put("新疆",
                        new String[]{"乌鲁木齐", "克拉玛依", "吐鲁番", "哈密", "阿克苏地区", "阿勒泰地区", "喀什地区", "和田地区", "昌吉回族自治州",
                                "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "克孜勒苏柯尔克孜自治州", "伊犁哈萨克自治州", "石河子", "阿拉尔", "图木舒克", "五家渠", "北屯",
                                "铁门关", "双河", "可克达拉", "昆玉", "兵团"});
            }
        });

        // 初始化信息类型与相关关键词对应关系映射表
        infoTypeKeywordMap = new LinkedHashMap<String, String[]>();
        infoTypeKeywordMap.put("工程", new String[]{"施工", "构筑物", "土石方", "组装", "装配", "打桩", "地基", "基础工程", "防水", "防腐",
                "混凝土", "钢结构", "砖石", "脚手架", "幕墙", "砌筑", "安装", "下水道", "铺设", "装修", "修缮"});
        infoTypeKeywordMap.put("服务",
                new String[]{"服务", "研究", "试验", "开发", "技术服务", "数据处理", "监理", "设计", "勘察", "测试", "咨询", "评估", "认证", "运行维护",
                        "运营", "传输", "互联网", "卫星", "租赁", "维修", "保养", "会议", "展览", "住宿", "餐饮", "商务", "法律", "公证", "仲裁", "调解",
                        "会计", "财务", "审计", "税务", "规划", "广告", "调查", "调研", "管理", "中介", "安全", "清洁", "包装", "印刷", "出版", "代理",
                        "旅游", "速递", "邮政", "分析", "测绘", "勘测", "勘探", "总承包", "生产", "金融", "担保", "保险"});
        infoTypeKeywordMap.put("货物", new String[]{"货物"});

        // 初始化行业分类与相关关键词对应关系映射表
        industryKeywordMap = new LinkedHashMap<String, String[]>();
        industryKeywordMap.put("交通运输",
                new String[]{"高速", "公路", "国道", "检测", "安全设施", "沥青", "路基", "桩基", "支线", "排水沟", "水泥", "道路", "路面", "城市道路",
                        "路养护", "路检测", "信号", "铁路", "高铁", "铁路物资", "地铁", "轨道", "交通", "立交桥", "机场", "水利枢纽", "港口", "码头", "泊位",
                        "渔港", "客运站", "交通枢纽", "隧道", "大桥"});
        industryKeywordMap.put("石油化工", new String[]{"中石油", "中石化", "中海油", "输气", "管道", "煤炭", "石油", "天然气", "石化", "石油化工",
                "煤化工", "辅机", "绝缘子", "液体输送", "橡胶塑料"});
        industryKeywordMap.put("水利水电",
                new String[]{"农田灌溉", "防护堤", "防洪堤", "水库", "引水", "蓄水", "提水", "南水北调", "三峡", "丹江口", "桥梁", "河道治理", "除险加固",
                        "山洪", "水电站", "水利", "桥", "饮水安全", "闸门", "监测", "启闭机", "河池", "河道", "水泵", "灌区水土保持", "水电", "交通桥",
                        "堤防", "泄洪", "防洪", "防涝", "桥梁检测", "护岸", "水利工程", "洪渠", "围堰", "供水管网", "防渗渠", "分水闸", "水利设施", "拦河",
                        "枢纽", "国网", "电力公司", "电厂", "电网", "南方电网", "尿素", "合成氨", "大唐", "神华", "华能", "华电", "电力投资", "国投电力",
                        "国华电力", "华润电力", "中广核", "二氧化碳", "东北电网", "金具", "自动化火电", "核电", "水电", "风电", "电网建设", "太阳能", "光伏发电",
                        "垃圾焚烧", "新能源", "节能改造"});
        industryKeywordMap.put("冶金矿产",
                new String[]{"选煤厂", "煤矿", "尾矿", "矿山", "矿产", "矿石", "水泥生产线", "混凝土", "金属", "冶炼", "黑色金属", "有色金属", "稀土",
                        "钢", "钢厂", "钢结构", "玻璃", "铁矿", "金矿", "铝", "煤", "水泥", "钢铁", "监理", "钢材", "昆钢", "铜", "钢球", "除尘器",
                        "白银", "冶金", "石材", "不锈钢", "锌", "合金", "原材料", "洗煤", "辅料", "石灰", "碳纤维", "原料", "太钢", "晋煤", "钢管",
                        "钢板", "烧结", "炼焦", "化学回收", "耐火材料", "炼铁", "炼钢", "轧钢", "制氧", "鼓风", "煤气"});
        industryKeywordMap.put("信息 自动化 电子电工",
                new String[]{"中国移动", "联通", "电信", "互联网", "雷达", "测控", "计算机", "信息", "网络", "通信", "监控系统", "电子自动化", "电子声像",
                        "电磁", "控制系统", "机房", "微电子", "光电子", "真空电子", "电子材料", "通信", "电源设备", "交换机", "光纤", "微波", "卫星", "地球站",
                        "移动通信", "接入网", "网管", "维护", "网络设备", "软件", "通信线路", "通信管道工程", "通信线缆", "通信线路", "综合布线", "广播电影",
                        "广播电视", "电影电视传输", "温度仪表", "压力仪表", "流量仪表", "物位检测仪表安", "显示仪表", "分析仪表", "仪表回路", "安全检测", "工业电视",
                        "信号报警", "钢管敷设工程", "工厂通信线路", "工厂通信设备", "供电系统", "仪表盘", "箱", "柜及附件安装工程", "仪表附件安装工程", "智能化", "集成系统",
                        "会议系统", "消防监控", "安防监控"});
        industryKeywordMap.put("机械机电",
                new String[]{"通风", "空调", "设备", "管道", "反应设备", "塔", "分离", "储存", "气柜工程", "工艺金属", "铝制", "铸铁", "PVC",
                        "玻璃钢", "聚酯", "锻压", "铸造", "风机", "泵", "压缩机", "起重机", "升降机", "缆索", "桅杆", "电梯", "锅炉", "卸煤设备", "煤场",
                        "碎煤", "上煤", "水力冲渣", "冲灰设备", "化学水处理汽轮发电机", "脱硫设备", "燃气-蒸汽", "空冷机", "水轮发电机", "抽水蓄能机", "水泵", "启闭机",
                        "核电", "压水堆", "重水堆", "高温气冷堆", "试验反应堆", "采煤机", "掘井机", "空压机", "井下通风", "排水", "排泥设备", "带式输送机",
                        "制板输送机安装工程"});
        industryKeywordMap.put("农林牧渔 环保绿化",
                new String[]{"林业", "农田", "山林", "鱼塘", "灌溉", "苗木", "果苗", "养殖业", "粮食", "豆类", "牲畜", "养殖", "农用品", "农用机械",
                        "饲料", "肥料", "农药", "种子", "园艺", "渔业", "粮油", "饲料加工", "屠宰", "农副产品", "木材", "农业", "灌溉", "退耕还林", "牧场",
                        "环境", "污水处理设备", "空气净化设备", "固废处理设备", "噪音防治设备", "环境监测设备", "消毒防腐设备", "节能降耗设备", "环卫清洁设备", "环保材料及药剂",
                        "环保仪器仪表", "废水", "废气", "废渣", "粉尘", "噪声", "放射性", "道路清扫保洁", "生活垃圾收集清运处理", "水务", "环保", "垃圾中转站",
                        "垃圾", "垃圾车", "垃圾焚烧", "视频监控", "环保局", "垃圾箱脱硫", "脱硝", "除尘", "除渣", "绿化养护", "苗木", "绿化带", "公园", "森林",
                        "景观", "节能", "风电", "可再生能源"});
        industryKeywordMap.put("医药 卫生 轻工食品",
                new String[]{"医院", "病房", "制药", "制剂", "临床检验分析仪器", "激光仪器设备手术室", "诊疗室设备及器具外伤处置车", "手术器械", "监护仪", "麻醉机",
                        "呼吸机", "血液细胞分析仪", "分化分析仪", "酶标仪", "洗板机", "尿液分析仪", "超声仪（彩超", "B超等）", "X线机", "核磁共振", "医用电子仪器设备",
                        "医用电梯", "医疗设备", "疫苗", "彩超", "心电图", "超声诊断", "呼吸机", "加压氧舱", "细胞分析仪", "教学设备", "化学发光", "pcr",
                        "蛋白纯化", "基因扩增", "脉冲场电泳", "高效液相", "层析", "血红蛋白", "粉碎机", "切片机", "炒药机", "煎药机", "压片机", "制丸机",
                        "多功能提取罐", "储液罐", "配液罐", "减压干燥箱", "可倾式反应锅", "胶囊灌装机", "泡罩式包装机", "颗粒包装机", "散剂包装机", "V型混合机",
                        "提升加料机", "速冻设备", "清洗设备", "干燥设备", "杀菌设备", "灌装设备", "水处理设备", "分选设备", "提取设备", "热交换设备", "传输设备",
                        "制浆造纸", "制糖", "制盐", "日化", "食品饮料", "化纤", "纺丝机", "织布机"});
        industryKeywordMap.put("建筑建材",
                new String[]{"公共厕所", "化粪池", "供热", "供水", "排水", "幕墙", "桩基", "拆除", "管道", "取暖", "空调", "消防", "安装", "装饰",
                        "装修", "门窗", "灯具", "砌块", "钢材", "水泥", "砂石", "梯", "总承包", "基础设施", "改扩建", "管网", "通风工程", "安置房", "铺装",
                        "防水", "灾后重建", "景观", "地下工程", "公共服务", "土地复垦", "管材", "管件", "PE", "PVC", "水泥", "玻璃", "陶瓷", "耐火材料",
                        "新型材料", "无机非金属材料"});
        industryKeywordMap.put("服装纺织",
                new String[]{"服装加工", "洗涤设备", "皮革加工", "纺织设备和器材", "纺织废料", "皮革废料", "纺织品", "服饰", "服装辅料", "工作服", "制服",
                        "特制服装", "牛仔服装", "皮革", "毛皮服装", "防寒服", "隔离服", "防尘服", "防酸服", "防火服", "抗油拒水服", "防静电服", "阻燃服",
                        "焊接防护服", "防电弧服", "纺织设备", "棉纺织设备", "麻纺织设备", "印染设备", "涤纶", "锦纶", "丙纶", "氨纶设备"});
        industryKeywordMap.put("体育 办公文教 旅游休闲",
                new String[]{"体育馆", "体育场", "游泳馆", "跳水馆", "体育公园", "田径场", "健身中心", "多功能房器械健身房", "办公家具", "病床", "剧场",
                        "影院桌椅", "实验室家具", "课桌", "椅", "保险柜", "档案柜", "密集架", "其他办公家具", "宾馆", "酒店用品", "纪念品", "游艺设施", "旅行服务",
                        "图书", "光学及照相器材", "办公耗材", "硒鼓", "墨盒", "墨粉", "色带", "教学模型", "用具", "教学设施", "实验室用品", "电话机", "可视电话",
                        "集团电话", "传真机", "复印机", "打印机", "投影机", "碎纸机", "绘图机", "晒图机", "视讯会议系统", "文艺设备", "舞台灯光音响设备", "摄影器材",
                        "录像设备"});
        industryKeywordMap.put("专业服务", new String[]{"胶制品", "广告", "出版", "音像", "广播", "电视", "排版", "制版设备", "印刷设备", "咨询",
                "监理", "设计", "维修", "保险", "租赁", "会议", "培训", "物业"});
    }

    /**
     * 从字符串中匹配出地区、省份和城市信息
     *
     * @param str 待匹配的字符串
     * @return 包含区域、省份和城市信息的对象（若区域或省份信息之中任意一个为空，则返回null）
     */
    public static PosOfProclamation judgePosOfProclamationWithoutCondition(String str) {
        boolean isJudgeFinished = false;
        PosOfProclamation posOfProclamation = new PosOfProclamation();
        Set<String> areaSet = areaProvinceCityMap.keySet();
        Iterator<String> areaIterator = areaSet.iterator();
        while (!isJudgeFinished && areaIterator.hasNext()) {
            String area = areaIterator.next();
            Map<String, String[]> provinceCityMap = areaProvinceCityMap.get(area);
            Set<String> provinceSet = provinceCityMap.keySet();
            Iterator<String> provinceIterator = provinceSet.iterator();
            while (!isJudgeFinished && provinceIterator.hasNext()) {
                String province = provinceIterator.next();
                String[] citySet = provinceCityMap.get(province);
                for (int i = 0; i < citySet.length && !isJudgeFinished; i++) {
                    if (str.contains(citySet[i])) {
                        posOfProclamation.setCity(citySet[i]);
                        posOfProclamation.setProvince(province);
                        posOfProclamation.setArea(area);
                        isJudgeFinished = true;
                    }
                }
                if (!isJudgeFinished) {
                    if (str.contains(province)) {
                        posOfProclamation.setProvince(province);
                        posOfProclamation.setArea(area);
                        isJudgeFinished = true;
                    }
                }
            }
        }

        // 匹配失败时（区域或省份两者之一匹配失败均认为整体匹配失败）将返回值重置为null
        if (posOfProclamation != null
                && (posOfProclamation.getArea() == null || posOfProclamation.getProvince() == null)) {
            posOfProclamation = null;
        }
        // 对全国唯一一个和省份同名的城市“吉林市”作特殊处理
        if (posOfProclamation != null && "吉林市".equals(posOfProclamation.getCity())) {
            posOfProclamation.setCity("吉林");
        }

        return posOfProclamation;
    }

    /**
     * 在指定条件下，从字符串中匹配出地区、省份和城市信息
     *
     * @param
     * @param str 待匹配的字符串
     * @return 包含区域、省份和城市信息的对象（若区域或省份信息之中任意一个为空，则返回null）
     */
    public static PosOfProclamation judgePosOfProclamation(PosOfProclamation condition, String str) {
        PosOfProclamation posOfProclamation = new PosOfProclamation();
        // condition不含有任何信息
        if (condition == null) {
            posOfProclamation = CheckProclamationUtil.judgePosOfProclamationWithoutCondition(str);
        }
        // condition不含有区域和省份信息（但可能含有城市信息）
        else if (condition.getArea() == null && condition.getProvince() == null) {
            if (condition.getCity() != null) {
                String city = condition.getCity();
                // 对全国唯一一个和省份同名的城市“吉林市”作特殊处理
                if ("吉林".equals(city)) {
                    city += "市";
                }
                posOfProclamation = CheckProclamationUtil.judgePosOfProclamationWithoutCondition(city);
            } else {
                posOfProclamation = CheckProclamationUtil.judgePosOfProclamationWithoutCondition(str);
            }
        }
        // condition至少包含区域、省份信息中的一个
        else {
            Map<String, String[]> provinceCityMap = null;
            String[] citySet = null;

            String area = condition.getArea();
            String province = condition.getProvince();
            if (area != null) {
                if (areaProvinceCityMap.containsKey(area)) {
                    posOfProclamation.setArea(area);
                    provinceCityMap = areaProvinceCityMap.get(area);
                }
            }
            if (province != null) {
                if (provinceCityMap != null) {
                    if (provinceCityMap.containsKey(province)) {
                        posOfProclamation.setProvince(province);
                        citySet = provinceCityMap.get(province);
                    }
                } else {
                    Set<Entry<String, Map<String, String[]>>> areaProvinceCityMapEntrySet = areaProvinceCityMap
                            .entrySet();
                    Iterator<Entry<String, Map<String, String[]>>> areaProvinceCityMapEntrySetIterator = areaProvinceCityMapEntrySet
                            .iterator();
                    while (citySet == null && areaProvinceCityMapEntrySetIterator.hasNext()) {
                        Entry<String, Map<String, String[]>> areaProvinceCityMapEntry = areaProvinceCityMapEntrySetIterator
                                .next();
                        provinceCityMap = areaProvinceCityMapEntry.getValue();
                        if (provinceCityMap != null && provinceCityMap.containsKey(province)) {
                            posOfProclamation.setArea(areaProvinceCityMapEntry.getKey());
                            posOfProclamation.setProvince(province);
                            citySet = provinceCityMap.get(province);
                        }
                    }
                }
            }

            /*
             * 若citySet非null 则说明当前condition提供的区域和省份信息取值均合法 以此为前提继续完成城市信息的匹配
             */
            if (citySet != null) {
                for (String city : citySet) {
                    if (str.contains(city)) {
                        posOfProclamation.setCity(city);
                        break;
                    }
                }
            }
            /*
             * 若citySet为null，但是provinceCityMap非null（且当前condition未提供省份信息）
             * 则说明当前condition提供的区域信息取值合法 以此为前提继续完成省份和城市的匹配
             */
            else if (provinceCityMap != null && condition.getProvince() == null) {
                boolean isJudgeFinished = false;
                Set<String> provinceSet = provinceCityMap.keySet();
                Iterator<String> provinceIterator = provinceSet.iterator();
                while (!isJudgeFinished && provinceIterator.hasNext()) {
                    String prov = provinceIterator.next();
                    citySet = provinceCityMap.get(prov);
                    for (int i = 0; i < citySet.length && !isJudgeFinished; i++) {
                        if (str.contains(citySet[i])) {
                            posOfProclamation.setCity(citySet[i]);
                            posOfProclamation.setProvince(prov);
                            isJudgeFinished = true;
                        }
                    }
                    if (!isJudgeFinished) {
                        if (str.contains(prov)) {
                            posOfProclamation.setProvince(prov);
                            isJudgeFinished = true;
                        }
                    }
                }
            }
            /*
             * 若citySet和provinceCityMap均为null，但是condition携带了信息
             * 则说明condition携带的区域和省份均有误 丢弃condition后再尝试匹配
             */
            else if (condition != null && (condition.getArea() != null || condition.getProvince() != null)) {
                posOfProclamation = CheckProclamationUtil.judgePosOfProclamationWithoutCondition(str);
            }
        }

        // 匹配失败时（区域或省份两者之一匹配失败均认为整体匹配失败）将返回值重置为null
        if (posOfProclamation != null
                && (posOfProclamation.getArea() == null || posOfProclamation.getProvince() == null)) {
            posOfProclamation = null;
        }
        // 对全国唯一一个和省份同名的城市“吉林市”作特殊处理
        if (posOfProclamation != null && "吉林市".equals(posOfProclamation.getCity())) {
            posOfProclamation.setCity("吉林");
        }

        return posOfProclamation;
    }

    /**
     * 对于从标题中匹配不到地区省份信息的公告，通过本方法可以获取默认地区省份信息
     * <p>
     * 注：若默认值不是全国，则可以通过覆盖本方法修改默认值
     * </p>
     *
     * @return 携带默认地区和省份信息的PosOfProclamation对象
     */
    public static PosOfProclamation getDefaultPosOfProclamation() {
        PosOfProclamation p = new PosOfProclamation();
        p.setArea("全国");
        p.setProvince("全国");
        p.setCity("全国");

        // 若有必要则可以通过下述代码提供区县的默认值 p.setDistrict("");

        return p;
    }

    /**
     * 修正公告的发布地区、省份和城市信息
     * <p>
     * 即将（全国，全国，null）的全部修正为（全国，全国，全国）<br>
     * 且将（某区域，某省份，null）的全部修正为（某区域，某省份，未知）
     * </p>
     *
     * @param p 封装了公告发布地区、省份、城市和区县信息的对象
     */
    public final static void revisePosOfProclamation(PosOfProclamation p) {
        if (p != null) {
            if ("全国".equals(p.getArea()) && "全国".equals(p.getProvince()) && p.getCity() == null) {
                p.setCity("全国");
            } else if (p.getCity() == null) {
                p.setCity("未知");
            }
        }
    }

    /**
     * 判断当前抓取到的公告是否有价值，没有则丢弃本公告
     *
     * @param title         公告标题
     * @param titleKeyWords 公告内容
     * @return 判断结果（true：有价值；false：无价值. 默认值为true）
     */
    public static boolean isProclamationValuable(String title, String[] titleKeyWords) {
        boolean isValuable = false;
        title = title.replaceAll("\\s*", "").trim();
        if (titleKeyWords != null && titleKeyWords.length > 0) {
            for (String titleKeyWord : titleKeyWords) {
                if (title.contains(titleKeyWord)) {
                    isValuable = true;
                    break;
                }
            }
        } else {
            isValuable = true;
        }
        if (isValuable) {
            if (judgeProclamationType(title) == ProclamationType.ZhongBiaoXinXi) {
                isValuable = false;
            }
        }
        return isValuable;
    }

    /**
     * 判断当前抓取到的公告是否包含无用字眼标题
     *
     * @param title         公告标题
     * @param titleKeyWords 公告内容
     * @return 判断结果（true：无价值；false：有价值. 默认值为false）
     */
    public static boolean isValuableByExceptTitleKeyWords(String title, String[] titleKeyWords) {
        boolean isValuable = false;
        for (String titleKeyWord : titleKeyWords) {
            if (title.contains(titleKeyWord)) {
                isValuable = true;
                break;
            }
        }
        return isValuable;
    }

    /**
     * 判断当前抓取到的公告是否有价值，没有则丢弃本公告
     *
     * @param title 公告标题
     * @return 判断结果（true：有价值；false：无价值. 默认值为true）
     */
    public static boolean isProclamationValuable(String title) {
        boolean isValuable = true;
        if (judgeProclamationType(title) != ProclamationType.ZhongBiaoXinXi) {
            isValuable = false;
        }
        return isValuable;
    }

    /**
     * 根据公告内容是否含有指定关键词来匹配行业分类信息
     *
     * @param content 待匹配公告的内容
     * @return 匹配到的行业分类信息
     */
    public static String judgeInfoType(String content) {
        StringBuilder infoTypeBuilder = new StringBuilder();
        String infoType = null;
        if (content != null && !content.isEmpty()) {
            Set<Entry<String, String[]>> infoTypeKeywordMapEntrySet = infoTypeKeywordMap.entrySet();
            for (Entry<String, String[]> infoTypeKeywordMapEntry : infoTypeKeywordMapEntrySet) {
                String singleInfoType = infoTypeKeywordMapEntry.getKey();
                String[] singleInfoTypeKeywords = infoTypeKeywordMapEntry.getValue();
                for (String singleInfoTypeKeyword : singleInfoTypeKeywords) {
                    if (content.contains(singleInfoTypeKeyword)) {
                        infoTypeBuilder.append(singleInfoType + ";");
                        break;
                    }
                }
            }
            infoType = infoTypeBuilder.toString();
            if (infoTypeBuilder.length() > 0) {
                infoType = infoType.substring(0, infoType.length() - 1);
            } else {
                infoType = "货物";
            }
        }
        return infoType;
    }

    /**
     * 根据公告内容是否含有指定关键词来匹配行业分类信息
     *
     * @param content 待匹配公告的内容
     * @return 匹配到的行业分类信息
     */
    public static String judgeIndustry(String content) {
        StringBuilder industryBuilder = new StringBuilder();
        String industry = null;
        if (content != null && !content.isEmpty()) {
            Set<Entry<String, String[]>> industryKeywordMapEntrySet = industryKeywordMap.entrySet();
            for (Entry<String, String[]> industryKeywordMapEntry : industryKeywordMapEntrySet) {
                String singleIndustry = industryKeywordMapEntry.getKey();
                String[] singleIndustryKeywords = industryKeywordMapEntry.getValue();
                for (String singleIndustryKeyword : singleIndustryKeywords) {
                    if (content.contains(singleIndustryKeyword)) {
                        industryBuilder.append(singleIndustry + ";");
                        break;
                    }
                }
            }
            industry = industryBuilder.toString();
            if (!industry.isEmpty()) {
                industry = industry.substring(0, industry.length() - 1);
            }
        }
        return industry;
    }

    /**
     * 校验公告的发布地信息是否合法
     *
     * @param area     待校验的地区信息
     * @param province 待校验的省份信息
     * @param city     待校验的城市信息
     * @return 是否合法（true：合法；false：不合法）
     */
    public static boolean isPosOfProclamationValidate(String area, String province, String city, String district) {
        boolean isValidate = true;

        // 若area取值确定（即非空），则需要校验area取值合法性，若area合法且province取值确定（即非空）时，还需要根据area来校验province取值合法性
        if (area != null && !area.isEmpty()) {
            if (!areaProvinceCityMap.containsKey(area)) {
                isValidate = false;
            } else if (province != null && !province.isEmpty()) {
                Map<String, String[]> provinceCityMap = areaProvinceCityMap.get(area);
                if (provinceCityMap == null || (provinceCityMap != null && !provinceCityMap.containsKey(province))) {
                    isValidate = false;
                }
            }
        }
        // 若area取值不确定，但province取值确定（即非空），则无需检验area取值合法性，只需要校验province取值合法性
        else if (province != null && !province.isEmpty()) {
            boolean foundMatchProvince = false;
            Set<Entry<String, Map<String, String[]>>> areaProvinceCityMapEntrySet = areaProvinceCityMap.entrySet();
            Iterator<Entry<String, Map<String, String[]>>> areaProvinceCityMapEntrySetIterator = areaProvinceCityMapEntrySet
                    .iterator();
            while (!foundMatchProvince && areaProvinceCityMapEntrySetIterator.hasNext()) {
                Entry<String, Map<String, String[]>> areaProvinceCityMapEntry = areaProvinceCityMapEntrySetIterator
                        .next();
                Map<String, String[]> provinceCityMap = areaProvinceCityMapEntry.getValue();
                if (provinceCityMap.containsKey(province)) {
                    foundMatchProvince = true;
                }
            }
            if (!foundMatchProvince) {
                isValidate = false;
            }
        }

        // 若城市取值确定（即非空），则需要校验城市取值合法性
        if (isValidate && city != null && !city.isEmpty()) {
            if (city.contains("标题") || city.contains("内容") || city.contains("匹配")) {
                isValidate = false;
            }
        }

        // 若区县取值确定（即非空），则需要校验区县取值合法性
        if (isValidate && district != null && !district.isEmpty()) {
            if (district.contains("标题") || district.contains("内容") || district.contains("匹配")) {
                isValidate = false;
            }
        }

        return isValidate;
    }

    /**
     * 根据公告标题判定公告类型（招标公告，中标公告，变更公告等）
     *
     * @param title 待判定类型公告的标题
     * @return 公告类型
     */

    public static ProclamationType judgeProclamationType(String title) {
        ProclamationType proclamationType = ProclamationType.Valueless;
        boolean findMatchedType = false;

        String[] s1 = {"招标"};
        String[] s2 = {"成交", "中标", "中选", "评标", "结果", "流标", "失败", "终止", "中止", "废标", "入围公示","入围结果","入围单位","入围候选人", "验收", "合同", "候选公示", "协议","入围名单","入围供应商名单","入围企业"};


        for (int i = 0; i < s2.length && !findMatchedType; i++) {
            if (title.contains(s2[i])) {
                proclamationType = ProclamationType.ZhongBiaoXinXi;
                findMatchedType = true;
            }
        }

        for (int i = 0; i < s1.length && !findMatchedType; i++) {
            if (title.contains(s1[i])) {
                proclamationType = ProclamationType.ZhaoBiaoGongGao;
                findMatchedType = true;
            }
        }

        return proclamationType;
    }

    // 以下为公告类型枚举类
    public static enum ProclamationType {
        ZhaoBiaoGongGao, ZhongBiaoXinXi, GongGaoBianGeng, ZhaoBiaoDaYi, KongZhiJia, Valueless
    }

    // 以下为公告发布地状态枚举类（用于表明公告的发布地信息是否合法）
    public static enum PosOfProclamationValidatingStatus {
        Legal, Illegal, UnKnown
    }

    // 以下为公告状态枚举类（用于表明公告记录是否合法）
    public static enum RecordStatus {
        Normal, HasNullRecordIdException, HasNullTitleException, HasIncompleteTitleException, HasUnreableTitleException, HasIllegalTitleException, HasNullContentException, HasNoChineseOrNoElementWithSrcAttrException, HasIllegalContentException, HasUnreableContentException, HasDateFormatException, HasIllegalAreaException, HasOverLengthContentException
    }

    // 以下为表名枚举类（用来检测入库表）
    public static enum TableName {
        T_ZHAO_BIAO_GONG_GAO, T_ZHONG_BIAO_XIN_XI, T_GONG_GAO_BIAN_GENG, T_ZHAO_BIAO_YU_GAO, T_ZHAO_BIAO_DA_YI, T_ZHAO_BIAO_WEN_JIAN, T_ZI_SHEN_JIE_GUO, T_KONG_ZHI_JIA
    }
    /*
     * public static void main(String[] args) { String area = "华北"; // 设置省份
     * String province = "北京"; // 设置城市 // 设置县 String district = "海淀区";
     *
     * //判断地区信息 PosOfProclamation postPro=new PosOfProclamation();
     * postPro.setArea(area); postPro.setCity(city);
     * postPro.setDistrict(district); postPro.setProvince(province);
     * PosOfProclamation
     * post=CheckProclamationUtil.judgePosOfProclamation(postPro,"测试数据");
     * if(post==null){ post=CheckProclamationUtil.getDefaultPosOfProclamation();
     * } logger.info(post.getArea()); logger.info(post.getProvince());
     * logger.info(post.getDistrict()); logger.info(post.getCity());
     *
     * }
     */

}
