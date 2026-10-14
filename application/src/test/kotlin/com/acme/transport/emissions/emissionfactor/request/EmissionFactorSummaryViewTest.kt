package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorSummary("code-450", "title-922")
        val view = toEmissionFactorSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorSummaryViews(mutableListOf<EmissionFactorSummary>(item, item)).size, "views")
    }
}
