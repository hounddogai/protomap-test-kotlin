package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRateCardPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRateCardPreference("code-695", "label-670")
        val view = toManualRateCardPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRateCardPreferenceViews(mutableListOf<ManualRateCardPreference>(item, item)).size, "views")
    }
}
