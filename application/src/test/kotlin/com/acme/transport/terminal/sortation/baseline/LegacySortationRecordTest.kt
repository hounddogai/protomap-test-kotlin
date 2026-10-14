package com.acme.transport.terminal.sortation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySortationRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacySortationRecord("code-89", "label-271")
        assertEquals(LegacySortationRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacySortationRecord("code-89", "label-271")
        try {
            item.moveTo(LegacySortationRecordStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySortationRecordStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacySortationRecord("code-89", "label-271")
        item.status = LegacySortationRecordStatus.STOPPED
        assertFails("move") { item.moveTo(LegacySortationRecordStatus.IDLE) }
    }
}
