package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRateCardEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRateCardEntry("code-238", "title-583")
        val view = toRollingRateCardEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRateCardEntryViews(mutableListOf<RollingRateCardEntry>(item, item)).size, "views")
    }
}
