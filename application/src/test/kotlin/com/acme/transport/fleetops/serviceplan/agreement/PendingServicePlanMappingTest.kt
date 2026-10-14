package com.acme.transport.fleetops.serviceplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingServicePlanMappingTest {
    @Test
    fun `starts in new`() {
        val item = newPendingServicePlanMapping("code-836", "label-85")
        assertEquals(PendingServicePlanMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPendingServicePlanMapping("code-836", "label-85")
        try {
            item.moveTo(PendingServicePlanMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingServicePlanMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPendingServicePlanMapping("code-836", "label-85")
        item.status = PendingServicePlanMappingStatus.FAILED
        assertFails("move") { item.moveTo(PendingServicePlanMappingStatus.NEW) }
    }
}
