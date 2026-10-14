package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingDeclarationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingDeclarationForecast("code-269", "title-655")
        val view = toPendingDeclarationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingDeclarationForecastViews(mutableListOf<PendingDeclarationForecast>(item, item)).size, "views")
    }
}
