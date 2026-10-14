package com.acme.transport.lastmile.redelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalRedeliveryScheduleTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalRedeliverySchedule("code-654", "label-871")
        assertEquals(RegionalRedeliveryScheduleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalRedeliverySchedule("code-654", "label-871")
        try {
            item.moveTo(RegionalRedeliveryScheduleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalRedeliveryScheduleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalRedeliverySchedule("code-654", "label-871")
        item.status = RegionalRedeliveryScheduleStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalRedeliveryScheduleStatus.OPEN) }
    }
}
