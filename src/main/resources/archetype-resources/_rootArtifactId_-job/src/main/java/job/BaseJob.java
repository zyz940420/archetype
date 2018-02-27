package ${package}.job;

import com.taobao.pamirs.schedule.IScheduleTaskDealMulti;
import com.taobao.pamirs.schedule.TaskItemDefine;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component("baseJob")
public class BaseJob implements IScheduleTaskDealMulti<String> {


    @Override
    public List<String> selectTasks(String taskParameter, String ownSign, int taskItemNum, List<TaskItemDefine> taskItemLists, int eachFetchDataNum) throws Exception {
        return null;
    }

    @Override
    public boolean execute(String[] strings, String s) throws Exception {
        return false;
    }

    @Override
    public Comparator<String> getComparator() {
        return null;
    }
}
