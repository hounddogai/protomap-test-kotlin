package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RateCardRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRateCardRule("code-423", "label-614")
        val view = toRateCardRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRateCardRuleViews(mutableListOf<RateCardRule>(item, item)).size, "views")
    }
}
