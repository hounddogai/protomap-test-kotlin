package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryInspectionAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryInspectionAgreement("code-115", "label-285")
        val view = toPrimaryInspectionAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryInspectionAgreementViews(mutableListOf<PrimaryInspectionAgreement>(item, item)).size, "views")
    }
}
