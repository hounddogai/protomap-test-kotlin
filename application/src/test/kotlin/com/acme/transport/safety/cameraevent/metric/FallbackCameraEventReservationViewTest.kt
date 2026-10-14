package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCameraEventReservationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCameraEventReservation("code-130", "title-173")
        val view = toFallbackCameraEventReservationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCameraEventReservationViews(mutableListOf<FallbackCameraEventReservation>(item, item)).size, "views")
    }
}
