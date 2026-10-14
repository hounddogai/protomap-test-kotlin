package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveRateCardPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveRateCardPlan("code-492", "name-475")
        val view = toAdaptiveRateCardPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveRateCardPlanViews(mutableListOf<AdaptiveRateCardPlan>(item, item)).size, "views")
    }
}
