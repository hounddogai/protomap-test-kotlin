package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newRedeliverySchedule("code-123", "name-818")
        assertEquals(RedeliveryScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRedeliverySchedule("code-123", "name-818")
        try {
            item.moveTo(RedeliveryScheduleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryScheduleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRedeliverySchedule("code-123", "name-818")
        item.status = RedeliveryScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RedeliveryScheduleStatus.OPEN) }
    }
}
