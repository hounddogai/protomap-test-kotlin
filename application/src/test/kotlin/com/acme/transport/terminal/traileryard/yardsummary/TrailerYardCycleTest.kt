package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerYardCycle("code-437", "title-953")
        assertEquals(TrailerYardCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTrailerYardCycle("code-437", "title-953")
        try {
            item.moveTo(TrailerYardCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerYardCycle("code-437", "title-953")
        item.status = TrailerYardCycleStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerYardCycleStatus.IDLE) }
    }
}
