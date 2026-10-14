package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newUtilizationAudit("code-809", "name-715")
        assertEquals(UtilizationAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newUtilizationAudit("code-809", "name-715")
        try {
            item.moveTo(UtilizationAuditStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationAuditStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newUtilizationAudit("code-809", "name-715")
        item.status = UtilizationAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(UtilizationAuditStatus.PLANNED) }
    }
}
