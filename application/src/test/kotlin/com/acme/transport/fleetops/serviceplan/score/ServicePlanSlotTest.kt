package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanSlotTest {
    @Test
    fun `starts in new`() {
        val item = newServicePlanSlot("code-113", "label-770")
        assertEquals(ServicePlanSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newServicePlanSlot("code-113", "label-770")
        try {
            item.moveTo(ServicePlanSlotStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanSlotStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newServicePlanSlot("code-113", "label-770")
        item.status = ServicePlanSlotStatus.FAILED
        assertFails("move") { item.moveTo(ServicePlanSlotStatus.NEW) }
    }
}
