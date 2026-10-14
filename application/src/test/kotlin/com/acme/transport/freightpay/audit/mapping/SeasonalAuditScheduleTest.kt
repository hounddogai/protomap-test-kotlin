package com.acme.transport.freightpay.audit.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAuditScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        assertEquals(SeasonalAuditScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        try {
            item.moveTo(SeasonalAuditScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAuditScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalAuditSchedule("code-221", "title-6")
        item.status = SeasonalAuditScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalAuditScheduleStatus.DRAFT) }
    }
}
