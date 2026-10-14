package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryEvChargeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryEvChargeAgreement("code-584", "name-6")
        val view = toPrimaryEvChargeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryEvChargeAgreementViews(mutableListOf<PrimaryEvChargeAgreement>(item, item)).size, "views")
    }
}
