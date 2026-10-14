package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingHubPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingHubPlan("code-986", "label-661")
        assertEquals(RollingHubPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingHubPlan("code-986", "label-661")
        try {
            item.moveTo(RollingHubPlanStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingHubPlanStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingHubPlan("code-986", "label-661")
        item.status = RollingHubPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingHubPlanStatus.PROPOSED) }
    }
}
