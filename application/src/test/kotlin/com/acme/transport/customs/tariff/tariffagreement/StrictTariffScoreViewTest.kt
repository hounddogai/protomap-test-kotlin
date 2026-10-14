package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTariffScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTariffScore("code-97", "title-64")
        val view = toStrictTariffScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTariffScoreViews(mutableListOf<StrictTariffScore>(item, item)).size, "views")
    }
}
