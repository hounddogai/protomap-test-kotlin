package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingEmissionFactorAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        val view = toRollingEmissionFactorAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingEmissionFactorAgreementViews(mutableListOf<RollingEmissionFactorAgreement>(item, item)).size, "views")
    }
}
