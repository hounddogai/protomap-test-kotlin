package com.acme.transport.terminal.traileryard.yardsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerYardMappingTest {
    @Test
    fun `starts in new`() {
        val item = newTrailerYardMapping("code-684", "name-69")
        assertEquals(TrailerYardMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTrailerYardMapping("code-684", "name-69")
        try {
            item.moveTo(TrailerYardMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerYardMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTrailerYardMapping("code-684", "name-69")
        item.status = TrailerYardMappingStatus.FAILED
        assertFails("move") { item.moveTo(TrailerYardMappingStatus.NEW) }
    }
}
