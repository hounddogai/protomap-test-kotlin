package com.acme.transport.fleetops.serviceplan.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newServicePlanException("code-134", "label-283")
        assertEquals(ServicePlanExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newServicePlanException("code-134", "label-283")
        try {
            item.moveTo(ServicePlanExceptionStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanExceptionStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServicePlanException("code-134", "label-283")
        item.status = ServicePlanExceptionStatus.FAILED
        assertFails("move") { item.moveTo(ServicePlanExceptionStatus.NEW) }
    }
}
