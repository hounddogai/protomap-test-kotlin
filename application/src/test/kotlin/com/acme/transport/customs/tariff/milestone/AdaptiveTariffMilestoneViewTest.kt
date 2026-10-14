package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTariffMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        val view = toAdaptiveTariffMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTariffMilestoneViews(mutableListOf<AdaptiveTariffMilestone>(item, item)).size, "views")
    }
}
