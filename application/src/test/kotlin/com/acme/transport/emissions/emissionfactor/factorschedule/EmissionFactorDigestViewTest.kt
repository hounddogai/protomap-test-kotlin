package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorDigest("code-418", "title-736")
        val view = toEmissionFactorDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorDigestViews(mutableListOf<EmissionFactorDigest>(item, item)).size, "views")
    }
}
