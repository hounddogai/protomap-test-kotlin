package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newAuditSchedule("code-396", "name-586")
        assertEquals(AuditScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAuditSchedule("code-396", "name-586")
        try {
            item.moveTo(AuditScheduleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditScheduleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAuditSchedule("code-396", "name-586")
        item.status = AuditScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(AuditScheduleStatus.CREATED) }
    }
}
