package sg.edu.ntu.taskflowapi.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.ntu.taskflowapi.model.Task;
import sg.edu.ntu.taskflowapi.service.TaskService;
import sg.edu.ntu.taskflowapi.service.TaskSummaryService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

	private final TaskService taskService;
	private final TaskSummaryService taskSummaryService;

	public TaskController(TaskService taskService, TaskSummaryService taskSummaryService) {
		this.taskService = taskService;
		this.taskSummaryService = taskSummaryService;
	}

	@GetMapping
	public List<Task> findAllTasks() {
		return taskService.findAllTasks();
	}

	@GetMapping("/{id}")
	public Task findTaskById(@PathVariable Long id) {
		return taskService.findTaskById(id);
	}

	@PostMapping
	public ResponseEntity<Task> createTask(@RequestBody Task task) {
		Task createdTask = taskService.createTask(task);
		return ResponseEntity.created(URI.create("/api/tasks/" + createdTask.getId())).body(createdTask);
	}

	@PutMapping("/{id}")
	public Task updateTask(@PathVariable Long id, @RequestBody Task task) {
		return taskService.updateTask(id, task);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
		taskService.deleteTask(id);
		return ResponseEntity.ok().build();
	}

	@PutMapping("/{id}/complete")
	public Task markTaskAsComplete(@PathVariable Long id) {
		return taskService.markTaskAsComplete(id);
	}

	@GetMapping(value = "/summary", produces = MediaType.TEXT_PLAIN_VALUE)
	public String summarizeTasks() {
		return taskSummaryService.summarizeTasks();
	}
}
