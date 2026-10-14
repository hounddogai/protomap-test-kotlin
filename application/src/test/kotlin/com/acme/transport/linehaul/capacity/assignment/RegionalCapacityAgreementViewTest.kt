package com.acme.transport.linehaul.capacity.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCapacityAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCapacityAgreement("code-56", "label-911")
        val view = toRegionalCapacityAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCapacityAgreementViews(mutableListOf<RegionalCapacityAgreement>(item, item)).size, "views")
    }
}
