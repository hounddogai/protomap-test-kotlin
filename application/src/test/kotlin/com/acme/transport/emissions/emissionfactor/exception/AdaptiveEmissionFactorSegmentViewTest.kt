package com.acme.transport.emissions.emissionfactor.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveEmissionFactorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveEmissionFactorSegment("code-10", "label-560")
        val view = toAdaptiveEmissionFactorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveEmissionFactorSegmentViews(mutableListOf<AdaptiveEmissionFactorSegment>(item, item)).size, "views")
    }
}
