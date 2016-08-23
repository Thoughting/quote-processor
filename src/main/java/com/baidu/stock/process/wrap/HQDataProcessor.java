package com.baidu.stock.process.wrap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import com.baidu.stock.indicator.concurrent.thread.Director;
import com.baidu.stock.indicator.concurrent.thread.EventHandler;
import com.baidu.stock.process.fetch.meta.HQSnapShot;

/**
 * 并行执行行情计算
 * @author dengjianli
 * 
 */
public class HQDataProcessor {
	private SnapshotHandler snapshotHandler;
    private ThreadPoolExecutor taskExecutor;
    
	public HQDataProcessor(ThreadPoolExecutor taskExecutor,SnapshotHandler snapshotHandler) {
		this.taskExecutor=taskExecutor;
		this.snapshotHandler=snapshotHandler;
	}

	@SuppressWarnings("unchecked")
	public TaskEvent doProcess(List<HQSnapShot> lstHQ) {
		// 并行计算
		Director<TaskEvent> director = new Director<HQDataProcessor.TaskEvent>(taskExecutor);
		StartTask start = new StartTask();
		MergeResultTask end=new MergeResultTask();
		int hqCount=lstHQ.size();
		//将集合行情数据分成5块
		if(hqCount<=100){
			List<HQSnapShot> lstHQA=lstHQ;
			Task_A taskA=new Task_A(lstHQA);
			director.begin(taskA);
		}else if(hqCount>100 && hqCount<=200){
			int chunckSize=hqCount/2;
			List<HQSnapShot> lstHQA=lstHQ.subList(0, chunckSize);
			List<HQSnapShot> lstHQB=lstHQ.subList(chunckSize,hqCount);
			Task_A taskA=new Task_A(lstHQA);
			Task_B taskB=new Task_B(lstHQB);
			director.begin(start).then(taskA);
			director.begin(start).then(taskB);
			director.after(taskA, taskB).then(end);
		}else{
			int chunckSize=hqCount/3;
			List<HQSnapShot> lstHQA=lstHQ.subList(0, chunckSize);
			List<HQSnapShot> lstHQB=lstHQ.subList(chunckSize, chunckSize*2);
			List<HQSnapShot> lstHQC=lstHQ.subList(chunckSize*2,hqCount);
			Task_A taskA=new Task_A(lstHQA);
			Task_B taskB=new Task_B(lstHQB);
			Task_C taskC=new Task_C(lstHQC);
			director.begin(start).then(taskA);
			director.begin(start).then(taskB);
			director.begin(start).then(taskC);
			director.after(taskA, taskB,taskC).then(end);
		}/*else{
			//并行5个批量任务处理
			int chunckSize=hqCount/5;
			List<HQSnapShot> lstHQA=lstHQ.subList(0, chunckSize);
			List<HQSnapShot> lstHQB=lstHQ.subList(chunckSize, chunckSize*2);
			List<HQSnapShot> lstHQC=lstHQ.subList(chunckSize*2, chunckSize*3);
			List<HQSnapShot> lstHQD=lstHQ.subList(chunckSize*3, chunckSize*4);
			List<HQSnapShot> lstHQE=lstHQ.subList(chunckSize*4, hqCount);
			Task_A taskA=new Task_A(lstHQA);
			Task_B taskB=new Task_B(lstHQB);
			Task_C taskC=new Task_C(lstHQC);
			Task_D taskD=new Task_D(lstHQD);
			Task_E taskE=new Task_E(lstHQE);
			director.begin(start).then(taskA);
			director.begin(start).then(taskB);
			director.begin(start).then(taskC);
			director.begin(start).then(taskD);
			director.begin(start).then(taskE);
			director.after(taskA, taskB, taskC, taskD,taskE).then(end);
		}*/
		director.ready();
		TaskEvent event = new TaskEvent();
		TaskEvent result = director.action(event,30000);
		return result;
	}

	public class TaskEvent {
		private ConcurrentHashMap<String,Set<HQSnapShot>>resultMap=new ConcurrentHashMap<String,Set<HQSnapShot>>();
		public ConcurrentHashMap<String, Set<HQSnapShot>> getResultMap() {
			return resultMap;
		}
	}

	class StartTask implements EventHandler<TaskEvent> {
		public void onEvent(TaskEvent event) {
			//do nothing
		}
		@Override
		public String toString() {
			return "StartTask";
		}
	}

	class Task_A implements EventHandler<TaskEvent> {
		private List<HQSnapShot> lstHQ;
		public Task_A(List<HQSnapShot> lstHQ){
			this.lstHQ=lstHQ;
		}
		public void onEvent(TaskEvent event) {
			for(HQSnapShot snapShot:lstHQ){
				HQSnapShot newSnapShot=snapshotHandler.handleRequest(snapShot);
				if(null!=newSnapShot){
					Set<HQSnapShot>snapShotSet_A= event.getResultMap().get("A");
					if(snapShotSet_A==null){
						snapShotSet_A=new HashSet<HQSnapShot>();
						snapShotSet_A.add(newSnapShot);
						event.getResultMap().putIfAbsent("A", snapShotSet_A);
					}else{
						snapShotSet_A.add(newSnapShot);
					}
				}
			}
		}
		@Override
		public String toString() {
			return "Task_A";
		}
	}
	class Task_B implements EventHandler<TaskEvent> {
		private List<HQSnapShot> lstHQ;
		public Task_B(List<HQSnapShot> lstHQ){
			this.lstHQ=lstHQ;
		}
		
		public void onEvent(final TaskEvent event) {
			for(final HQSnapShot snapShot:lstHQ){
				HQSnapShot newSnapShot=snapshotHandler.handleRequest(snapShot);
				if(null!=newSnapShot){
					Set<HQSnapShot>snapShotSet_B= event.getResultMap().get("B");
					if(snapShotSet_B==null){
						snapShotSet_B=new HashSet<HQSnapShot>();
						snapShotSet_B.add(newSnapShot);
						event.getResultMap().putIfAbsent("B", snapShotSet_B);
					}else{
						snapShotSet_B.add(newSnapShot);
					}
				}
			}
		}
		@Override
		public String toString() {
			return "Task_B";
		}
	}
	class Task_C implements EventHandler<TaskEvent> {
		private List<HQSnapShot> lstHQ;
		public Task_C(List<HQSnapShot> lstHQ){
			this.lstHQ=lstHQ;
		}
		
		public void onEvent(TaskEvent event) {
			for(HQSnapShot snapShot:lstHQ){
				HQSnapShot newSnapShot=snapshotHandler.handleRequest(snapShot);
				if(null!=newSnapShot){
					Set<HQSnapShot>snapShotSet_C= event.getResultMap().get("C");
					if(snapShotSet_C==null){
						snapShotSet_C=new HashSet<HQSnapShot>();
						snapShotSet_C.add(newSnapShot);
						event.getResultMap().putIfAbsent("C", snapShotSet_C);
					}else{
						snapShotSet_C.add(newSnapShot);
					}
				}
			}
		}
		@Override
		public String toString() {
			return "Task_C";
		}
	}
//	class Task_D implements EventHandler<TaskEvent> {
//		private List<HQSnapShot> lstHQ;
//		public Task_D(List<HQSnapShot> lstHQ){
//			this.lstHQ=lstHQ;
//		}
//		public void onEvent(TaskEvent event) {
//			for(HQSnapShot snapShot:lstHQ){
//				HQSnapShot newSnapShot=snapshotHandler.handleRequest(snapShot);
//				if(null!=newSnapShot){
//					Set<HQSnapShot>snapShotSet_D= event.getResultMap().get("D");
//					if(snapShotSet_D==null){
//						snapShotSet_D=new HashSet<HQSnapShot>();
//						snapShotSet_D.add(newSnapShot);
//						event.getResultMap().putIfAbsent("D", snapShotSet_D);
//					}else{
//						snapShotSet_D.add(newSnapShot);
//					}
//				}
//			}
//		}
//		@Override
//		public String toString() {
//			return "Task_D";
//		}
//	}
//
//	class Task_E implements EventHandler<TaskEvent> {
//		private List<HQSnapShot> lstHQ;
//		public Task_E(List<HQSnapShot> lstHQ){
//			this.lstHQ=lstHQ;
//		}
//		public void onEvent(TaskEvent event) {
//			for(HQSnapShot snapShot:lstHQ){
//				HQSnapShot newSnapShot=snapshotHandler.handleRequest(snapShot);
//				if(null!=newSnapShot){
//					Set<HQSnapShot>snapShotSet_E= event.getResultMap().get("E");
//					if(snapShotSet_E==null){
//						snapShotSet_E=new HashSet<HQSnapShot>();
//						snapShotSet_E.add(newSnapShot);
//						event.getResultMap().putIfAbsent("E", snapShotSet_E);
//					}else{
//						snapShotSet_E.add(newSnapShot);
//					}
//				}
//			}
//		}
//		@Override
//		public String toString() {
//			return "Task_E";
//		}
//	}
	
	static class MergeResultTask implements EventHandler<TaskEvent> {
		public void onEvent(TaskEvent event) {
			//nothing to do
		}

		@Override
		public String toString() {
			return "MergeResultTask";
		}
	}
}