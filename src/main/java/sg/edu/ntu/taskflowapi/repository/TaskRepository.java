package sg.edu.ntu.taskflowapi.repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import sg.edu.ntu.taskflowapi.model.Task;

@Repository
public class TaskRepository {

	private final Map<Long, Task> tasks = new HashMap<>();
	private long nextId = 1;

	public synchronized List<Task> findAll() {
		return tasks.values().stream()
				.sorted(Comparator.comparing(Task::getId))
				.map(this::copy)
				.toList();
	}

	public synchronized Optional<Task> findById(Long id) {
		return Optional.ofNullable(tasks.get(id)).map(this::copy);
	}

	public synchronized Task save(Task task) {
		Task savedTask = copy(task);
		if (savedTask.getId() == null) {
			savedTask.setId(nextId++);
		}
		tasks.put(savedTask.getId(), savedTask);
		return copy(savedTask);
	}

	public synchronized boolean deleteById(Long id) {
		return tasks.remove(id) != null;
	}

	private Task copy(Task task) {
		return new Task(task.getId(), task.getTitle(), task.isCompleted());
	}
}
