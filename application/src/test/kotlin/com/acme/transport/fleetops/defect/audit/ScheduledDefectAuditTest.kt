package com.acme.transport.fleetops.defect.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledDefectAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledDefectAudit("code-581", "label-908")
        assertEquals(ScheduledDefectAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newScheduledDefectAudit("code-581", "label-908")
        try {
            item.moveTo(ScheduledDefectAuditStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledDefectAuditStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledDefectAudit("code-581", "label-908")
        item.status = ScheduledDefectAuditStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledDefectAuditStatus.DRAFT) }
    }
}
