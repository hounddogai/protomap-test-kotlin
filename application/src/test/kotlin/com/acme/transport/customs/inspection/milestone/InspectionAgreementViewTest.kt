package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class InspectionAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newInspectionAgreement("code-60", "title-59")
        val view = toInspectionAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toInspectionAgreementViews(mutableListOf<InspectionAgreement>(item, item)).size, "views")
    }
}
