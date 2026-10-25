package sg.edu.ntu.taskflowapi.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import sg.edu.ntu.taskflowapi.model.Task;
import sg.edu.ntu.taskflowapi.repository.TaskRepository;

@Service
public class TaskService {

	private final TaskRepository taskRepository;

	public TaskService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public List<Task> findAllTasks() {
		return taskRepository.findAll();
	}

	public Task findTaskById(Long id) {
		return taskRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + id));
	}

	public Task createTask(Task task) {
		validateTitle(task);
		return taskRepository.save(new Task(null, task.getTitle(), task.isCompleted()));
	}

	public Task updateTask(Long id, Task task) {
		Task existingTask = findTaskById(id);
		validateTitle(task);
		existingTask.setTitle(task.getTitle());
		existingTask.setCompleted(task.isCompleted());
		return taskRepository.save(existingTask);
	}

	public void deleteTask(Long id) {
		if (!taskRepository.deleteById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + id);
		}
	}

	public Task markTaskAsComplete(Long id) {
		Task task = findTaskById(id);
		task.setCompleted(true);
		return taskRepository.save(task);
	}

	private void validateTitle(Task task) {
		if (task.getTitle() == null || task.getTitle().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title must not be blank");
		}
	}
}
