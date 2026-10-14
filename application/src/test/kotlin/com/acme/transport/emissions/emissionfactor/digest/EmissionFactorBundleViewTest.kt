package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorBundle("code-91", "title-102")
        val view = toEmissionFactorBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorBundleViews(mutableListOf<EmissionFactorBundle>(item, item)).size, "views")
    }
}
