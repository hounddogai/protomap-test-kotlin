package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEmissionFactorMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        val view = toAdaptiveEmissionFactorMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEmissionFactorMilestoneViews(mutableListOf<AdaptiveEmissionFactorMilestone>(item, item)).size, "views")
    }
}
