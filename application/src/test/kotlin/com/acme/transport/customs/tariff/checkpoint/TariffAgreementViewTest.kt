package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffAgreement("code-639", "label-80")
        val view = toTariffAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffAgreementViews(mutableListOf<TariffAgreement>(item, item)).size, "views")
    }
}
