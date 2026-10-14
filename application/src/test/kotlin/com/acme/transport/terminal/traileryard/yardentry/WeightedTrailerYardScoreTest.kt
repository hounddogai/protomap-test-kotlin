package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerYardScoreTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        assertEquals(WeightedTrailerYardScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        try {
            item.moveTo(WeightedTrailerYardScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerYardScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedTrailerYardScore("code-352", "title-572")
        item.status = WeightedTrailerYardScoreStatus.FAILED
        assertFails("move") { item.moveTo(WeightedTrailerYardScoreStatus.NEW) }
    }
}
