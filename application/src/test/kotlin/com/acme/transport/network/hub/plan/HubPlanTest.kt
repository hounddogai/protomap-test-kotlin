package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubPlan("code-650", "name-229")
        assertEquals(HubPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newHubPlan("code-650", "name-229")
        try {
            item.moveTo(HubPlanStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubPlanStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubPlan("code-650", "name-229")
        item.status = HubPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubPlanStatus.PROPOSED) }
    }
}
