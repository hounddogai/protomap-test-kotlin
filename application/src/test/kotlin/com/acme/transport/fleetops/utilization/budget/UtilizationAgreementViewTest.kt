package com.acme.transport.fleetops.utilization.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationAgreement("code-352", "title-38")
        val view = toUtilizationAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationAgreementViews(mutableListOf<UtilizationAgreement>(item, item)).size, "views")
    }
}
