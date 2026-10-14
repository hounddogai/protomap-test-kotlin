package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetAssignmentTest {
    @Test
    fun `starts in open`() {
        val item = newRouteOffsetAssignment("code-338", "label-411")
        assertEquals(RouteOffsetAssignmentStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRouteOffsetAssignment("code-338", "label-411")
        try {
            item.moveTo(RouteOffsetAssignmentStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetAssignmentStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRouteOffsetAssignment("code-338", "label-411")
        item.status = RouteOffsetAssignmentStatus.ARCHIVED
        assertFails("move") { item.moveTo(RouteOffsetAssignmentStatus.OPEN) }
    }
}
