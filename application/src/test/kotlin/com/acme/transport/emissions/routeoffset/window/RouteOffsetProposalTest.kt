package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetProposalTest {
    @Test
    fun `starts in planned`() {
        val item = newRouteOffsetProposal("code-433", "title-530")
        assertEquals(RouteOffsetProposalStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newRouteOffsetProposal("code-433", "title-530")
        try {
            item.moveTo(RouteOffsetProposalStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetProposalStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRouteOffsetProposal("code-433", "title-530")
        item.status = RouteOffsetProposalStatus.CANCELLED
        assertFails("move") { item.moveTo(RouteOffsetProposalStatus.PLANNED) }
    }
}
