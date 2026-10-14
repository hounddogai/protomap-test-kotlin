package com.acme.transport.freightpay.accessorial.accessorialentry

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialForecast("code-23", "title-434")
        val view = toAccessorialForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialForecastViews(mutableListOf<AccessorialForecast>(item, item)).size, "views")
    }
}
