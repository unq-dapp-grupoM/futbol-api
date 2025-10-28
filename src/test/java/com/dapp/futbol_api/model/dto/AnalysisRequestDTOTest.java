package com.dapp.futbol_api.model.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AnalysisRequestDTOTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        // Arrange
        AnalysisRequestDTO dto = new AnalysisRequestDTO();

        // Act
        dto.setPlayerName("Lionel Messi");
        dto.setOpponent("Real Madrid");
        dto.setIsHome(true);
        dto.setPosition("Forward");

        // Assert
        assertEquals("Lionel Messi", dto.getPlayerName());
        assertEquals("Real Madrid", dto.getOpponent());
        assertTrue(dto.getIsHome());
        assertEquals("Forward", dto.getPosition());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        // Arrange & Act
        AnalysisRequestDTO dto = new AnalysisRequestDTO("Cristiano Ronaldo", "Barcelona", false, "Striker");

        // Assert
        assertEquals("Cristiano Ronaldo", dto.getPlayerName());
        assertEquals("Barcelona", dto.getOpponent());
        assertFalse(dto.getIsHome());
        assertEquals("Striker", dto.getPosition());
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        AnalysisRequestDTO dto1 = new AnalysisRequestDTO("Neymar Jr.", "PSG", true, "Winger");
        AnalysisRequestDTO dto2 = new AnalysisRequestDTO("Neymar Jr.", "PSG", true, "Winger");
        AnalysisRequestDTO dto3 = new AnalysisRequestDTO("Kylian Mbappé", "PSG", true, "Winger");
        AnalysisRequestDTO dto4 = new AnalysisRequestDTO("Neymar Jr.", "Monaco", true, "Winger");
        AnalysisRequestDTO dto5 = new AnalysisRequestDTO("Neymar Jr.", "PSG", false, "Winger");
        AnalysisRequestDTO dto6 = new AnalysisRequestDTO("Neymar Jr.", "PSG", true, "Midfielder");

        // Assert
        assertEquals(dto1, dto2, "Objects with same properties should be equal.");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Hashcodes of equal objects should be the same.");

        assertNotEquals(dto1, dto3, "Objects with different playerName should not be equal.");
        assertNotEquals(dto1, dto4, "Objects with different opponent should not be equal.");
        assertNotEquals(dto1, dto5, "Objects with different isHome should not be equal.");
        assertNotEquals(dto1, dto6, "Objects with different position should not be equal.");
        assertNotEquals(null, dto1, "Object should not be equal to null.");
        assertNotEquals(dto1, new Object(), "Object should not be equal to an object of a different class.");
    }

    @Test
    void testToString() {
        // Arrange
        AnalysisRequestDTO dto = new AnalysisRequestDTO("Luka Modric", "Atletico Madrid", true, "Midfielder");

        // Act
        String dtoAsString = dto.toString();

        // Assert
        assertTrue(dtoAsString.contains("playerName=Luka Modric"), "toString should contain playerName.");
        assertTrue(dtoAsString.contains("opponent=Atletico Madrid"), "toString should contain opponent.");
        assertTrue(dtoAsString.contains("isHome=true"), "toString should contain isHome.");
        assertTrue(dtoAsString.contains("position=Midfielder"), "toString should contain position.");
    }
}