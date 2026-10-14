package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RouteOffsetReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newRouteOffsetReservation("code-376", "name-386")
        assertEquals(RouteOffsetReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRouteOffsetReservation("code-376", "name-386")
        try {
            item.moveTo(RouteOffsetReservationStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RouteOffsetReservationStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRouteOffsetReservation("code-376", "name-386")
        item.status = RouteOffsetReservationStatus.CLOSED
        assertFails("move") { item.moveTo(RouteOffsetReservationStatus.DRAFT) }
    }
}
