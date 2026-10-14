package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newSortationRecord("code-490", "label-354")
        assertEquals(SortationRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newSortationRecord("code-490", "label-354")
        try {
            item.moveTo(SortationRecordStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationRecordStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newSortationRecord("code-490", "label-354")
        item.status = SortationRecordStatus.STOPPED
        assertFails("move") { item.moveTo(SortationRecordStatus.IDLE) }
    }
}
