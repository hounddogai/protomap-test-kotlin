package com.acme.transport.fleetops.serviceplan.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServicePlanQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newLenientServicePlanQuota("code-404", "label-386")
        assertEquals(LenientServicePlanQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLenientServicePlanQuota("code-404", "label-386")
        try {
            item.moveTo(LenientServicePlanQuotaStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServicePlanQuotaStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientServicePlanQuota("code-404", "label-386")
        item.status = LenientServicePlanQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientServicePlanQuotaStatus.OPEN) }
    }
}
