//当前值被改变了之后显示当前选取的值
$(".select").change(function () {
    var value = $(".select>option:selected").val();
    alert("当前选择的值为：" + value);
});


//全部选取复选框
$(":button:first").click(function () {

    $(":checkbox").prop("checked", "checked");
});
//全部取消选取复选框
$(":button:eq(1)").click(function () {
    $(":checkbox").prop("checked", false);
});
//全部选取和取消复选框
$("button:eq(2)").click(function () {
    $("input[type='checkbox']").prop("checked", function (index, val) {
        return !val;
    });
});
//把已选取的复选框移除
$("input[type='button']").click(function () {
    $(":checkbox:checked").each(function () {
        $(this).parents("tr").remove();
    });
});
//===========================加载文件之后出现的值==============================================
$("input[type='file']").on('change', function () {
    // var files = $("#fileInput").prop('files');
    var fileInput = document.getElementById("fileInput");
    var file = fileInput.files[0];
    var name = file.name;
    console.log(file.name);
    $("#file").hide();
    var url = "/data";
    var args = {"fileName": name, "time": new Date()}
    $.post(url, args, function (info) {
        // console.log(info);
        $("#relation").empty().append(info.relation.relationName);
        $("#attributes").empty().append(info.relation.numAttributes);
        $("#instance").empty().append(info.relation.numInstance);
        $("#weights").empty().append(info.relation.sumOfWeights);
        var attrLen = info.attributes.length;
        for (var i = 0; i < attrLen; i++) {
            $(".attrTable tr td").remove();
        }
        for (var i = 0; i < attrLen; i++) {
            $(".attrTable").append("<tr><td>" + (i + 1) + "</td><td>" + "<input type='checkbox'>" + "</td><td class=\"attrValue\"   value=" + i + ">" + (info.attributes[i]) + "</td></tr>>");
        }
        $(".name").empty().append(info.attributes[0]);
        $(".type").empty().append(info.selectedAttrbute.type);
        $(".missing").empty().append(info.selectedAttrbute.MissingValue[0]);
        $(".missPer").empty().append(info.selectedAttrbute.Missing[0]);
        $(".distinct").empty().append(info.selectedAttrbute.Distinct[0]);
        $(".unique").empty().append(info.selectedAttrbute.Unique[0]);
        $(".uniquePer").empty().append(info.selectedAttrbute.UniPercent[0]);
        // debugger;
        if (info.selectedAttrbute.isDigit) {
            $(".secondTable").hide("slow");
            $(".firstTable").show("slow",function () {
                // var staLen = info.statistic.listSta.length;
                $("#tdMin").empty().append(info.statistic.listSta[0]);
                $("#tdMax").empty().append(info.statistic.listSta[1]);
                $("#tdMean").empty().append(info.statistic.listSta[2]);
                $("#tdStd").empty().append(info.statistic.listSta[3]);

                // for (var m = 0; m < staLen; m++) {
                $(".tdMin").empty().append(info.statistic.listMin[0]);
                $(".tdMax").empty().append(info.statistic.listMax[0]);
                $(".tdMean").empty().append(info.statistic.listMean[0]);
                $(".tdStd").empty().append(info.statistic.listStdDev[0]);

            });


        } else {
            $(".firstTable").hide("slow");
            $(".secondTable").show("slow",function () {
                var label = info.chartData.tuple_1;
                var count = info.chartData.tuple_2;
                var len = label.length;
                for (var i = 0; i < len; i++) {
                    var data1 = label[i];
                    if(data1=='?'){
                        data1 = "NaN";
                    }
                    var data2 = count[i];
                    $(".secondTable").append("<tr><td>" + i + "</td><td>" + data1 + "</td><td>" + data2 + "</td><td>" + data2 + "</td></tr>");
                }
            });


        }
        $(function () {
            var myChart = echarts.init(document.getElementById('barChart'));
            myChart.clear();
            myChart.showLoading();
            var names = []; //放x轴内容
            var nums = []; //放y轴内容
            var xValue = info.chartData.tuple_1;
            var xLength = xValue.length;
            var yValue = info.chartData.tuple_2;
            var yLength = yValue.length;
            var attrName = info.chartData.attrName;
            for (var i = 0; i < xLength; i++) {
                if(xValue[i]=='?'){
                    xValue[i] = "NaN";
                }
                names.push(xValue[i]);
            }
            for (var j = 0; j < yLength; j++) {
                nums.push(yValue[j]);
            }
            var option = ({
                title: {
                    text: attrName
                },
                toolbox: {
                    feature: {
                        dataView: {
                            show: true
                        }
                    }
                },
                xAxis: {
                    data: names
                },
                tooltip: {},
                legend: {
                    data: [attrName]
                },
                yAxis: {},
                series: [{
                    type: 'bar',
                    name: attrName,
                    data: nums,
                    markPoint: {
                        data: [
                            {type: 'max',name: '最大值'},
                            {type: 'min',name: '最小值'}
                        ]
                    },
                    markLine:{
                        data: [
                            {type: 'average',name: '平均线'}
                        ]
                    }
                }]
            });
            myChart.hideLoading();
            myChart.setOption(option);
        });

        // getData(data);
    });

});
//========================根据索引改变值===============================
$(document).on("click", "td", function () {
    var $val = $(this).attr("value");
    var url = "/varIndex";
    var args = {"index": $val, "time": new Date()};
    //alert(val);
    $.post(url, args, function (data) {

        var name = data.selectedAttrbute.name;
        $(".name").empty().append(name);

        var type = data.selectedAttrbute.type;
        $(".type").empty().append(type);

        var uniPercent = data.selectedAttrbute.uniPercent;
        $(".uniquePer").empty().append(uniPercent);

        var unique = data.selectedAttrbute.unique;
        $(".unique").empty().append(unique);

        var missingValue = data.selectedAttrbute.missingValue;
        $(".missing").empty().append(missingValue);

        var missing = data.selectedAttrbute.missing;
        $(".missPer").empty().append(missing);

        var distinct = data.selectedAttrbute.distinct;
        $(".distinct").empty().append(distinct);
        if (data.statistic.isDigit) {//显示到第一个表
            $(".secondTable").hide();
            $(".firstTable").show("slow",function () {
                debugger;
                $("#tdMin").empty().append(data.statistic.listSta[0]);
                $("#tdMax").empty().append(data.statistic.listSta[1]);
                $("#tdMean").empty().append(data.statistic.listSta[2]);
                $("#tdStd").empty().append(data.statistic.listSta[3]);
                var min = data.statistic.min;
                $(".tdMin").empty().append(min);
                var max = data.statistic.max;
                $(".tdMax").empty().append(max);
                var mean = data.statistic.mean;
                $(".tdMean").empty().append(mean);
                var stdDev = data.statistic.stdDev;
                $(".tdStd").empty().append(stdDev);

            });

        } else {//显示第二个表
            $(".firstTable").hide();
            $(".secondTable").show("slow",function () {
                var label = data.chartData.tuple_1;
                var count = data.chartData.tuple_2;
                var len = label.length;
                for (var i = 0; i < len; i++) {
                    $(".secondTable tr td").remove();
                }
                for (var i = 0; i < len; i++) {
                    var data1 = $(label).get(i);
                    if(data1=='?'){
                        data1 = "NaN";
                    }
                    var data2 = $(count).get(i);
                    $(".secondTable").append("<tr><td>" + i + "</td><td>" + data1 + "</td><td>" + data2 + "</td><td>" + data2 + "</td></tr>");
                }

            });


        }
        $(function () {
            var myChart = echarts.init(document.getElementById('barChart'));
            myChart.clear();
            myChart.showLoading();
            var names = []; //放x轴内容
            var nums = []; //放y轴内容
            var xValue = data.chartData.tuple_1;//取出tuple_1的值（数组）
            var xLength = xValue.length;
            var yValue = data.chartData.tuple_2;//取出tuple_2的值（数组）
            var yLength = yValue.length;
            var attrName = data.chartData.attrName;//属性名
            for (var i = 0; i < xLength; i++) {
                if(xValue[i]=='?'){
                    xValue[i] = "NaN";
                }
                names.push(xValue[i]);
            }
            for (var j = 0; j < yLength; j++) {
                nums.push(yValue[j]);
            }
            var option = ({
                title: {
                    text: attrName
                },
                toolbox: {
                    feature: {
                        dataView: {
                            show: true//数据展示
                        }
                    }
                },
                xAxis: {
                    data: names
                },
                tooltip: {},
                legend: {
                    data: [attrName]
                },
                yAxis: {},
                series: [{
                    type: 'bar',
                    name: attrName,
                    data: nums,
                    markPoint: {
                        data:
                            [
                                {type: 'max', name: '最大值'},
                                {type: 'min', name: '最小值'}
                            ]
                    },
                    markLine: {
                        data: [
                            {type: 'average', name: '平均线'}
                        ]
                    }

                }]
            });
            myChart.hideLoading();
            myChart.setOption(option);
        });

        return false;
    }, "JSON");
})
