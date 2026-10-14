package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedUtilizationAuditTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedUtilizationAudit("code-589", "title-36")
        assertEquals(WeightedUtilizationAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedUtilizationAudit("code-589", "title-36")
        try {
            item.moveTo(WeightedUtilizationAuditStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedUtilizationAuditStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedUtilizationAudit("code-589", "title-36")
        item.status = WeightedUtilizationAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedUtilizationAuditStatus.OPEN) }
    }
}
