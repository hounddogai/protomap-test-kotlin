package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredEmissionFactorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredEmissionFactorSegment("code-363", "name-63")
        val view = toTieredEmissionFactorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredEmissionFactorSegmentViews(mutableListOf<TieredEmissionFactorSegment>(item, item)).size, "views")
    }
}
