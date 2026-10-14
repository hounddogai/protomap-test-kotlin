package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetAllocationTest {
    @Test
    fun `starts in proposed`() {
        val item = newRouteOffsetAllocation("code-907", "title-69")
        assertEquals(RouteOffsetAllocationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRouteOffsetAllocation("code-907", "title-69")
        try {
            item.moveTo(RouteOffsetAllocationStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetAllocationStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRouteOffsetAllocation("code-907", "title-69")
        item.status = RouteOffsetAllocationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RouteOffsetAllocationStatus.PROPOSED) }
    }
}
