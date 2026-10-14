package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorRequest("code-897", "label-664")
        val view = toEmissionFactorRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorRequestViews(mutableListOf<EmissionFactorRequest>(item, item)).size, "views")
    }
}
