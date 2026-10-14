package com.acme.transport.linehaul.capacity.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCapacityExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCapacityException("code-477", "name-913")
        val view = toSeasonalCapacityExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCapacityExceptionViews(mutableListOf<SeasonalCapacityException>(item, item)).size, "views")
    }
}
