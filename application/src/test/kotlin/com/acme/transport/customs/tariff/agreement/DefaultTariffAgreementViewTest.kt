package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTariffAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTariffAgreement("code-607", "name-747")
        val view = toDefaultTariffAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTariffAgreementViews(mutableListOf<DefaultTariffAgreement>(item, item)).size, "views")
    }
}
