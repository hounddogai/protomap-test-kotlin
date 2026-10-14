package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServicePlanRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        assertEquals(FallbackServicePlanRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        try {
            item.moveTo(FallbackServicePlanRollupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServicePlanRollupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackServicePlanRollup("code-232", "label-883")
        item.status = FallbackServicePlanRollupStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackServicePlanRollupStatus.DRAFT) }
    }
}
