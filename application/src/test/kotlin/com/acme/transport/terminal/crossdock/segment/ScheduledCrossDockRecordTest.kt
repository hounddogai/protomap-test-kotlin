package com.acme.transport.terminal.crossdock.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledCrossDockRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledCrossDockRecord("code-943", "title-260")
        assertEquals(ScheduledCrossDockRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newScheduledCrossDockRecord("code-943", "title-260")
        try {
            item.moveTo(ScheduledCrossDockRecordStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledCrossDockRecordStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledCrossDockRecord("code-943", "title-260")
        item.status = ScheduledCrossDockRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledCrossDockRecordStatus.PLANNED) }
    }
}
