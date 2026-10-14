package com.acme.transport.fleetops.serviceplan.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanDraftTest {
    @Test
    fun `starts in planned`() {
        val item = newServicePlanDraft("code-261", "label-965")
        assertEquals(ServicePlanDraftStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newServicePlanDraft("code-261", "label-965")
        try {
            item.moveTo(ServicePlanDraftStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanDraftStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newServicePlanDraft("code-261", "label-965")
        item.status = ServicePlanDraftStatus.CANCELLED
        assertFails("move") { item.moveTo(ServicePlanDraftStatus.PLANNED) }
    }
}
