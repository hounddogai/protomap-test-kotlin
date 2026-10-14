package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newSortationMapping("code-241", "title-340")
        assertEquals(SortationMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newSortationMapping("code-241", "title-340")
        try {
            item.moveTo(SortationMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSortationMapping("code-241", "title-340")
        item.status = SortationMappingStatus.STOPPED
        assertFails("move") { item.moveTo(SortationMappingStatus.IDLE) }
    }
}
