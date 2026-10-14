package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventReservation("code-104", "title-163")
        val view = toCameraEventReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventReservationViews(mutableListOf<CameraEventReservation>(item, item)).size, "views")
    }
}
