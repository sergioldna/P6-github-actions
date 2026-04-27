package com.uma.example.springuma.integration;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;

public class MedicoControllerMockMvcIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico medico;

    @BeforeEach
    void setUp() {
        medico = new Medico();
        medico.setId(1L);
        medico.setDni("835");
        medico.setNombre("Miguel");
        medico.setEspecialidad("Ginecologia");
    }

    private void crearMedico(Medico medico) throws Exception {
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());
    }

    private void actualizarMedico(Medico medico, long id, String dni, String nombre, String especialidad) throws Exception {
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        medico.setDni(dni);
        medico.setEspecialidad(especialidad);
        medico.setId(id);
        medico.setNombre(nombre);

        this.mockMvc.perform(put("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(get("/medico"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))  // comprueba que el tamanyo sea 1
                .andExpect(jsonPath("$[0]").value(medico));

    }

    private void eliminarMedico(Medico medico) throws Exception {
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(delete("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isOk());
    }

    private void obtenerMedico(Medico medico) throws Exception {
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(get("/medico"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(1)))  // comprueba que el tamanyo sea 1
                .andExpect(jsonPath("$[0]").value(medico));
    }

    @Test
    @DisplayName("Crear un médico")
    void crearMedico() throws Exception {
        crearMedico(medico);
    }

    @Test
    @DisplayName("Actualizar un médico")
    void actualizarMedico() throws Exception {
        actualizarMedico(medico, 2L, "836", "Sergio", "Consulta");
    }

    @Test
    @DisplayName("Obtener un médico")
    void obtenerMedico() throws Exception {
        obtenerMedico(medico);
    }

    @Test
    @DisplayName("Eliminar un médico")
    void eliminarMedico() throws Exception {
        eliminarMedico(medico);
    }
}
