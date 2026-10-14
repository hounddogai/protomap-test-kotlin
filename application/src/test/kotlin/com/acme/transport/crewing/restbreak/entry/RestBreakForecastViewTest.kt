package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakForecast("code-640", "title-637")
        val view = toRestBreakForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakForecastViews(mutableListOf<RestBreakForecast>(item, item)).size, "views")
    }
}
