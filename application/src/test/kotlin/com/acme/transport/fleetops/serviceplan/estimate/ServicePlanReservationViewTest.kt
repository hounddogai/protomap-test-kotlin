package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanReservation("code-794", "name-655")
        val view = toServicePlanReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanReservationViews(mutableListOf<ServicePlanReservation>(item, item)).size, "views")
    }
}
