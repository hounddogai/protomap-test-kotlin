package com.acme.transport.emissions.idling.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledIdlingAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledIdlingAudit("code-177", "label-702")
        assertEquals(ScheduledIdlingAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledIdlingAudit("code-177", "label-702")
        try {
            item.moveTo(ScheduledIdlingAuditStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledIdlingAuditStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledIdlingAudit("code-177", "label-702")
        item.status = ScheduledIdlingAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledIdlingAuditStatus.PROPOSED) }
    }
}
