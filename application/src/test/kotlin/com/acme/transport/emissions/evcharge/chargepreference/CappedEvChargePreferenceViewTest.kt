package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedEvChargePreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedEvChargePreference("code-401", "label-450")
        val view = toCappedEvChargePreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedEvChargePreferenceViews(mutableListOf<CappedEvChargePreference>(item, item)).size, "views")
    }
}
