package com.acme.transport.network.servicearea.areamanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaMilestoneTest {
    @Test
    fun `starts in created`() {
        val item = newServiceAreaMilestone("code-603", "title-535")
        assertEquals(ServiceAreaMilestoneStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newServiceAreaMilestone("code-603", "title-535")
        try {
            item.moveTo(ServiceAreaMilestoneStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaMilestoneStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newServiceAreaMilestone("code-603", "title-535")
        item.status = ServiceAreaMilestoneStatus.RETIRED
        assertFails("move") { item.moveTo(ServiceAreaMilestoneStatus.CREATED) }
    }
}
