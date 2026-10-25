package sg.edu.ntu.taskflowapi;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import sg.edu.ntu.taskflowapi.model.Task;
import sg.edu.ntu.taskflowapi.repository.TaskRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key-not-a-real-secret",
		"spring.ai.model.embedding=none"
})
@AutoConfigureMockMvc
class TaskflowApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TaskRepository taskRepository;

	@MockitoBean
	private ChatModel chatModel;

	@BeforeEach
	void clearTasks() {
		taskRepository.findAll().forEach(task -> taskRepository.deleteById(task.getId()));
		when(chatModel.getOptions()).thenReturn(ChatOptions.builder().build());
	}

	@Test
	void returnsAnEmptyTaskList() throws Exception {
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(content().json("[]"));
	}

	@Test
	void createsReadsUpdatesCompletesAndDeletesATask() throws Exception {
		String response = mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id":999,"title":"Learn Spring Boot"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Learn Spring Boot"))
				.andExpect(jsonPath("$.completed").value(false))
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(response).get("id").asLong();
		assertThat(id).isNotEqualTo(999);
		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id));
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
		mockMvc.perform(put("/api/tasks/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"id":888,"title":"Submit assignment","completed":false}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.title").value("Submit assignment"))
				.andExpect(jsonPath("$.completed").value(false));
		mockMvc.perform(put("/api/tasks/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
		mockMvc.perform(put("/api/tasks/{id}/complete", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.completed").value(true));
		mockMvc.perform(delete("/api/tasks/{id}", id))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/tasks"))
				.andExpect(content().json("[]"));
	}

	@Test
	void creationReturnsLocationAndHonorsCompletedFlag() throws Exception {
		String response = mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Already finished","completed":true}
								"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/tasks/[0-9]+")))
				.andExpect(jsonPath("$.completed").value(true))
				.andReturn().getResponse().getContentAsString();
		long id = objectMapper.readTree(response).get("id").asLong();
		mockMvc.perform(get("/api/tasks/{id}", id))
				.andExpect(status().isOk());
	}

	@Test
	void returnsNotFoundForAllOperationsOnMissingTasks() throws Exception {
		mockMvc.perform(get("/api/tasks/999"))
				.andExpect(status().isNotFound());
		mockMvc.perform(put("/api/tasks/999")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Missing task","completed":false}
								"""))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/tasks/999"))
				.andExpect(status().isNotFound());
		mockMvc.perform(put("/api/tasks/999/complete"))
				.andExpect(status().isNotFound());
		assertThat(taskRepository.findAll()).isEmpty();
	}

	@Test
	void rejectsBlankOrMissingTitlesWithoutChangingStoredTasks() throws Exception {
		for (String body : List.of("{}", "{\"title\":\"   \"}")) {
			mockMvc.perform(post("/api/tasks")
							.contentType(MediaType.APPLICATION_JSON).content(body))
					.andExpect(status().isBadRequest());
		}
		Task task = taskRepository.save(new Task(null, "Original title", false));
		mockMvc.perform(put("/api/tasks/{id}", task.getId())
						.contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
				.andExpect(status().isBadRequest());
		assertThat(taskRepository.findById(task.getId()).orElseThrow().getTitle()).isEqualTo("Original title");
	}

	@Test
	void rejectsMalformedJsonAndNonNumericIds() throws Exception {
		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON).content("{"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/tasks/not-a-number"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void summaryReturnsPlainTextAndSendsBothTaskStatusesToTheModel() throws Exception {
		taskRepository.save(new Task(null, "Pending {task}", false));
		taskRepository.save(new Task(null, "Finished task", true));
		String summary = "Pending task is pending. Finished task is done.";
		when(chatModel.call(any(Prompt.class)))
				.thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(summary)))));

		mockMvc.perform(get("/api/tasks/summary"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
				.andExpect(content().string(summary));

		ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
		verify(chatModel).call(prompt.capture());
		String userText = prompt.getValue().getUserMessage().getText();
		JsonNode sentTasks = objectMapper.readTree(userText.substring(userText.indexOf('\n') + 1));
		assertThat(sentTasks.size()).isEqualTo(2);
		assertThat(sentTasks.get(0).get("title").stringValue()).isEqualTo("Pending {task}");
		assertThat(sentTasks.get(0).get("completed").asBoolean()).isFalse();
		assertThat(sentTasks.get(1).get("completed").asBoolean()).isTrue();
	}

	@Test
	void summaryHandlesAnEmptyTaskList() throws Exception {
		when(chatModel.call(any(Prompt.class)))
				.thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("There are no tasks.")))));
		mockMvc.perform(get("/api/tasks/summary"))
				.andExpect(status().isOk())
				.andExpect(content().string("There are no tasks."));
		ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
		verify(chatModel).call(prompt.capture());
		assertThat(prompt.getValue().getUserMessage().getText()).endsWith("[]");
	}

	@Test
	void summaryRejectsAnEmptyModelResponse() throws Exception {
		for (ChatResponse response : List.of(
				new ChatResponse(List.of()),
				new ChatResponse(List.of(new Generation(new AssistantMessage("   ")))))) {
			when(chatModel.call(any(Prompt.class))).thenReturn(response);
			mockMvc.perform(get("/api/tasks/summary"))
					.andExpect(status().isBadGateway());
		}
	}

}
