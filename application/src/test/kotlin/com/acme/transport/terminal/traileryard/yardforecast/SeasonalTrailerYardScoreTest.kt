package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardScoreTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalTrailerYardScore("code-456", "title-798")
        assertEquals(SeasonalTrailerYardScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalTrailerYardScore("code-456", "title-798")
        try {
            item.moveTo(SeasonalTrailerYardScoreStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardScoreStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalTrailerYardScore("code-456", "title-798")
        item.status = SeasonalTrailerYardScoreStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalTrailerYardScoreStatus.CREATED) }
    }
}
