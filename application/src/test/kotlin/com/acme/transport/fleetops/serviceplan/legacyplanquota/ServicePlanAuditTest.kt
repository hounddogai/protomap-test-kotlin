package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanAudit("code-509", "name-829")
        assertEquals(ServicePlanAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newServicePlanAudit("code-509", "name-829")
        try {
            item.moveTo(ServicePlanAuditStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanAuditStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanAudit("code-509", "name-829")
        item.status = ServicePlanAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanAuditStatus.PLANNED) }
    }
}
