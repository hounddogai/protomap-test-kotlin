package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetMilestoneTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackRouteOffsetMilestone("code-5", "title-326")
        assertEquals(FallbackRouteOffsetMilestoneStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newFallbackRouteOffsetMilestone("code-5", "title-326")
        try {
            item.moveTo(FallbackRouteOffsetMilestoneStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetMilestoneStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackRouteOffsetMilestone("code-5", "title-326")
        item.status = FallbackRouteOffsetMilestoneStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackRouteOffsetMilestoneStatus.REQUESTED) }
    }
}
