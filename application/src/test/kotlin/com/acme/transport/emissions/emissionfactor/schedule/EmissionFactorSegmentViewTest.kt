package com.acme.transport.emissions.emissionfactor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorSegment("code-167", "name-928")
        val view = toEmissionFactorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorSegmentViews(mutableListOf<EmissionFactorSegment>(item, item)).size, "views")
    }
}
