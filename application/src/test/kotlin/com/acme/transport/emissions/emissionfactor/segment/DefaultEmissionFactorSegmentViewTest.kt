package com.acme.transport.emissions.emissionfactor.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultEmissionFactorSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultEmissionFactorSegment("code-851", "label-140")
        val view = toDefaultEmissionFactorSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultEmissionFactorSegmentViews(mutableListOf<DefaultEmissionFactorSegment>(item, item)).size, "views")
    }
}
