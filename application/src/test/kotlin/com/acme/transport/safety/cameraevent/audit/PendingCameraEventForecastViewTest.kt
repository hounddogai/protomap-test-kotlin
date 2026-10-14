package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCameraEventForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCameraEventForecast("code-800", "title-432")
        val view = toPendingCameraEventForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCameraEventForecastViews(mutableListOf<PendingCameraEventForecast>(item, item)).size, "views")
    }
}
