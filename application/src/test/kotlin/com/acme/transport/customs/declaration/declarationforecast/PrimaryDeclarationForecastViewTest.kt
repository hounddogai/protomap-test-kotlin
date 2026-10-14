package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDeclarationForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDeclarationForecast("code-353", "name-16")
        val view = toPrimaryDeclarationForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDeclarationForecastViews(mutableListOf<PrimaryDeclarationForecast>(item, item)).size, "views")
    }
}
