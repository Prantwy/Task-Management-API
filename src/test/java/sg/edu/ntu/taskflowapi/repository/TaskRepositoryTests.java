package sg.edu.ntu.taskflowapi.repository;

import org.junit.jupiter.api.Test;

import sg.edu.ntu.taskflowapi.model.Task;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRepositoryTests {

	private final TaskRepository repository = new TaskRepository();

	@Test
	void startsEmptyAndGeneratesUniqueIdsWithoutReusingDeletedIds() {
		assertThat(repository.findAll()).isEmpty();
		Task first = repository.save(new Task(null, "First", false));
		Task second = repository.save(new Task(null, "Second", false));
		repository.deleteById(second.getId());
		Task third = repository.save(new Task(null, "Third", false));

		assertThat(first.getId()).isEqualTo(1L);
		assertThat(second.getId()).isEqualTo(2L);
		assertThat(third.getId()).isEqualTo(3L);
		assertThat(repository.findAll()).extracting(Task::getId).containsExactly(1L, 3L);
		assertThat(repository.findById(second.getId())).isEmpty();
	}

	@Test
	void updatesAnExistingTaskWithoutCreatingAnotherTask() {
		Task task = repository.save(new Task(null, "Original", false));
		task.setTitle("Updated");
		task.setCompleted(true);
		repository.save(task);

		assertThat(repository.findAll()).hasSize(1);
		Task savedTask = repository.findById(task.getId()).orElseThrow();
		assertThat(savedTask.getTitle()).isEqualTo("Updated");
		assertThat(savedTask.isCompleted()).isTrue();
	}

	@Test
	void taskChangesMustBeExplicitlySaved() {
		Task input = new Task(null, "Original", false);
		Task savedTask = repository.save(input);
		input.setTitle("Changed input");
		savedTask.setTitle("Changed result");
		repository.findById(savedTask.getId()).orElseThrow().setCompleted(true);
		repository.findAll().getFirst().setTitle("Changed list");

		Task storedTask = repository.findById(savedTask.getId()).orElseThrow();
		assertThat(storedTask.getTitle()).isEqualTo("Original");
		assertThat(storedTask.isCompleted()).isFalse();
	}

	@Test
	void deletingAMissingTaskReportsFailure() {
		assertThat(repository.deleteById(99L)).isFalse();
	}
}
