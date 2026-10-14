package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRateCardPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRateCardPreference("code-910", "name-591")
        val view = toGlobalRateCardPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRateCardPreferenceViews(mutableListOf<GlobalRateCardPreference>(item, item)).size, "views")
    }
}
