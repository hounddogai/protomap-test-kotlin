package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetReservation("code-376", "name-386")
        val view = toRouteOffsetReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetReservationViews(mutableListOf<RouteOffsetReservation>(item, item)).size, "views")
    }
}
