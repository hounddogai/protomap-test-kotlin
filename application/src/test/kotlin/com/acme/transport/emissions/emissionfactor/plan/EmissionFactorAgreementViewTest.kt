package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorAgreement("code-477", "title-65")
        val view = toEmissionFactorAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorAgreementViews(mutableListOf<EmissionFactorAgreement>(item, item)).size, "views")
    }
}
