package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalFuelSurchargePreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalFuelSurchargePreference("code-26", "label-335")
        val view = toSeasonalFuelSurchargePreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalFuelSurchargePreferenceViews(mutableListOf<SeasonalFuelSurchargePreference>(item, item)).size, "views")
    }
}
