package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictZoneRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictZoneRecord("code-389", "title-353")
        assertEquals(StrictZoneRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newStrictZoneRecord("code-389", "title-353")
        try {
            item.moveTo(StrictZoneRecordStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictZoneRecordStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictZoneRecord("code-389", "title-353")
        item.status = StrictZoneRecordStatus.STOPPED
        assertFails("move") { item.moveTo(StrictZoneRecordStatus.IDLE) }
    }
}
