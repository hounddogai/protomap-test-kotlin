package com.acme.transport.fleetops.serviceplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newServicePlanBatch("code-636", "label-123")
        assertEquals(ServicePlanBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newServicePlanBatch("code-636", "label-123")
        try {
            item.moveTo(ServicePlanBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServicePlanBatch("code-636", "label-123")
        item.status = ServicePlanBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(ServicePlanBatchStatus.REQUESTED) }
    }
}
