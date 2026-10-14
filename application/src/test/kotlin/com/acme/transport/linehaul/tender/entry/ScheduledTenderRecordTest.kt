package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledTenderRecordTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledTenderRecord("code-753", "title-343")
        assertEquals(ScheduledTenderRecordStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newScheduledTenderRecord("code-753", "title-343")
        try {
            item.moveTo(ScheduledTenderRecordStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledTenderRecordStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledTenderRecord("code-753", "title-343")
        item.status = ScheduledTenderRecordStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledTenderRecordStatus.IDLE) }
    }
}
