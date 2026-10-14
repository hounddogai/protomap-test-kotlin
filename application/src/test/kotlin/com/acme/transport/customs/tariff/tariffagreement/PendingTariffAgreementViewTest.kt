package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingTariffAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingTariffAgreement("code-336", "title-185")
        val view = toPendingTariffAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingTariffAgreementViews(mutableListOf<PendingTariffAgreement>(item, item)).size, "views")
    }
}
