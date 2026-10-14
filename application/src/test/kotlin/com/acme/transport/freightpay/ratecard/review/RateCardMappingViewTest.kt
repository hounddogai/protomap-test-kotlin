package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardMapping("code-94", "name-457")
        val view = toRateCardMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardMappingViews(mutableListOf<RateCardMapping>(item, item)).size, "views")
    }
}
