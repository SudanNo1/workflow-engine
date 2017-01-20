package org.radrso.workflow.wfservice.controller;

import lombok.extern.log4j.Log4j;
import org.radrso.workflow.entities.config.WorkflowConfig;
import org.radrso.workflow.entities.wf.WorkflowExecuteStatus;
import org.radrso.workflow.wfservice.service.WorkflowExecuteStatusService;
import org.radrso.workflow.wfservice.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by raomengnan on 17-1-20.
 */
@RestController
@RequestMapping(value = "/workflow")
@Log4j
public class WorkflowConfroller {

    @Autowired
    private WorkflowService workflowService;
    @Autowired
    private WorkflowExecuteStatusService statusService;

    @RequestMapping(method = RequestMethod.PUT, value = "/create")
    public ResponseEntity<ModelMap> create(WorkflowConfig workflow){
        boolean res = workflowService.save(workflow);
        ModelMap map = new ModelMap();
        map.put("status", res);
        if(!res) {
            map.put("msg", "请检查后重试");
            return new ResponseEntity<ModelMap>(map, HttpStatus.BAD_REQUEST);
        }else
            workflowService.updateServiceStatus(workflow);

        return new ResponseEntity<ModelMap>(map, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/update")
    public ResponseEntity<ModelMap> update(WorkflowConfig workflow){

        boolean res = false;
        if(workflow.getId() != null
                && workflowService.getByWorkflowId(workflow.getId()) != null)
            res = workflowService.save(workflow);

        ModelMap map = new ModelMap();
        map.put("status", res);
        if(!res) {
            map.put("msg", "更新失败");
            return new ResponseEntity<ModelMap>(map, HttpStatus.BAD_REQUEST);
        }else
            workflowService.updateServiceStatus(workflow);

        return new ResponseEntity<ModelMap>(map, HttpStatus.OK);
    }

    @RequestMapping("/{id}")
    public WorkflowConfig getWorkflowById(@PathVariable("id") String id){
        System.out.println("[Debug]"+id);
        return workflowService.getByWorkflowId(id);
    }

    @RequestMapping("/app/{application}")
    public List<WorkflowConfig> getWorkflowByApplication(@PathVariable("application") String application){
        return workflowService.getByApplication(application);
    }

    @RequestMapping(value = "/delete/{workflowid}", method = RequestMethod.DELETE)
    public ResponseEntity<ModelMap> delete(@PathVariable("workflowid") String workflowid){
        boolean res = workflowService.delete(workflowid);
        ModelMap map = new ModelMap();
        map.put("status", res);
        if(!res){
            map.put("msg", String.format("Delete [%s] error", workflowid));
            return new ResponseEntity<ModelMap>(map, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<ModelMap>(map, HttpStatus.OK);
    }

    @RequestMapping(value = "/deletel/app/{application}", method = RequestMethod.DELETE)
    public ResponseEntity<ModelMap> deleteByApplication(@PathVariable("application") String application){
        boolean res = workflowService.deleteByApplication(application);
        ModelMap map = new ModelMap();
        map.put("status", res);
        if(!res){
            map.put("msg", String.format("Delete by [%s] error", application));
            return new ResponseEntity<ModelMap>(map, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<ModelMap>(map, HttpStatus.OK);
    }

    @RequestMapping("/status/{workflowid}")
    public WorkflowExecuteStatus getWorkflowStatus(@PathVariable("workflowid") String workflowid){
        workflowService.updateServiceStatus(workflowService.getByWorkflowId(workflowid));
        return statusService.get(workflowid);
    }

    @RequestMapping(value = "/upload/jar", method = RequestMethod.POST)
    public ResponseEntity<ModelMap> uploadJar(String application, MultipartFile file){
        log.info(String.format("Upload jar file [%s]", file));
        boolean res = workflowService.transferJarFile(application, file);
        ModelMap map = new ModelMap();
        map.put("status", res);

        if(!res){
            map.put("msg", "Upload error");
            return new ResponseEntity<ModelMap>(map, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<ModelMap>(map, HttpStatus.OK);
    }
}
