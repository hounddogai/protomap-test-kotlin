package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedEmissionFactorDraftViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedEmissionFactorDraft("code-616", "label-668")
        val view = toCappedEmissionFactorDraftView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedEmissionFactorDraftViews(mutableListOf<CappedEmissionFactorDraft>(item, item)).size, "views")
    }
}
