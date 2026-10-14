package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEmissionFactorAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        val view = toFallbackEmissionFactorAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEmissionFactorAgreementViews(mutableListOf<FallbackEmissionFactorAgreement>(item, item)).size, "views")
    }
}
