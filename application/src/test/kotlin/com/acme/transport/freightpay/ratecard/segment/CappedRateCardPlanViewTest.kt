package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRateCardPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRateCardPlan("code-231", "name-81")
        val view = toCappedRateCardPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRateCardPlanViews(mutableListOf<CappedRateCardPlan>(item, item)).size, "views")
    }
}
