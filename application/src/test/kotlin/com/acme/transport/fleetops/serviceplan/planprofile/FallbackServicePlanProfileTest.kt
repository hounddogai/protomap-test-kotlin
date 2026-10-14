package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackServicePlanProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackServicePlanProfile("code-982", "label-372")
        assertEquals(FallbackServicePlanProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFallbackServicePlanProfile("code-982", "label-372")
        try {
            item.moveTo(FallbackServicePlanProfileStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackServicePlanProfileStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackServicePlanProfile("code-982", "label-372")
        item.status = FallbackServicePlanProfileStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackServicePlanProfileStatus.DRAFT) }
    }
}
