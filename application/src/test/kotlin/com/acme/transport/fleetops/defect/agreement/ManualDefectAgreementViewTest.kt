package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDefectAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDefectAgreement("code-749", "title-743")
        val view = toManualDefectAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDefectAgreementViews(mutableListOf<ManualDefectAgreement>(item, item)).size, "views")
    }
}
