package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardGroup("code-365", "title-686")
        val view = toRateCardGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardGroupViews(mutableListOf<RateCardGroup>(item, item)).size, "views")
    }
}
