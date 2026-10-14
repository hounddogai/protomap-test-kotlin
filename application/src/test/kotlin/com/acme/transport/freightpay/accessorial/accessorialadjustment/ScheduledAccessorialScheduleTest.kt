package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAccessorialScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledAccessorialSchedule("code-442", "title-711")
        assertEquals(ScheduledAccessorialScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newScheduledAccessorialSchedule("code-442", "title-711")
        try {
            item.moveTo(ScheduledAccessorialScheduleStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAccessorialScheduleStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledAccessorialSchedule("code-442", "title-711")
        item.status = ScheduledAccessorialScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledAccessorialScheduleStatus.DRAFT) }
    }
}
