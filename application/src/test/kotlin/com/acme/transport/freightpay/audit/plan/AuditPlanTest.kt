package com.acme.transport.freightpay.audit.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newAuditPlan("code-263", "name-707")
        assertEquals(AuditPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAuditPlan("code-263", "name-707")
        try {
            item.moveTo(AuditPlanStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditPlanStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAuditPlan("code-263", "name-707")
        item.status = AuditPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(AuditPlanStatus.PLANNED) }
    }
}
