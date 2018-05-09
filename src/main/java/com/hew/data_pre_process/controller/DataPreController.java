package com.hew.data_pre_process.controller;

import com.hew.data_pre_process.service.DataInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hw
 * @date 2018/5/9 12:58
 */
@Controller
public class DataPreController {
    @Autowired
    private DataInstance instance;

    @RequestMapping({"/"})
    public String Index() {
        return "dataPreProcess";
    }

    @RequestMapping(
            value = {"/data"},
            method = {RequestMethod.POST}
    )
    @ResponseBody
    public Map HomePage(@RequestParam("fileName") String fileName) {
        Map<String, Object> result = new HashMap();
        this.instance.getFileName(fileName);
        Map relation = this.instance.getRelation();
        List attributes = this.instance.getAttributes();
        Map selectedAttrbute = this.instance.getSelectedAttrbute();
        Map statistic = this.instance.getStatistic();
        Map chartData = this.instance.getVarData(0);
        result.put("relation", relation);
        result.put("attributes", attributes);
        result.put("selectedAttrbute", selectedAttrbute);
        result.put("statistic", statistic);
        result.put("chartData", chartData);
        return result;
    }

    @PostMapping({"/varIndex"})
    @ResponseBody
    public Map getVarIndex(@RequestParam("index") int varIndex) {
        Map<String, Object> result = new HashMap();
        Map selectedAttrbute = this.instance.getSelectedAttrbuteVar(varIndex);
        Map statistic = this.instance.getStatisticVar(varIndex);
        Map varData = this.instance.getVarData(varIndex);
        result.put("selectedAttrbute", selectedAttrbute);
        result.put("statistic", statistic);
        result.put("chartData", varData);
        return result;
    }
}
