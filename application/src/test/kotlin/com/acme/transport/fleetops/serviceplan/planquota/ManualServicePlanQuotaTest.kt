package com.acme.transport.fleetops.serviceplan.planquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualServicePlanQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newManualServicePlanQuota("code-790", "label-95")
        assertEquals(ManualServicePlanQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newManualServicePlanQuota("code-790", "label-95")
        try {
            item.moveTo(ManualServicePlanQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualServicePlanQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualServicePlanQuota("code-790", "label-95")
        item.status = ManualServicePlanQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualServicePlanQuotaStatus.OPEN) }
    }
}
