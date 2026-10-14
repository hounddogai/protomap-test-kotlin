package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeAgreement("code-932", "title-270")
        val view = toEvChargeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeAgreementViews(mutableListOf<EvChargeAgreement>(item, item)).size, "views")
    }
}
