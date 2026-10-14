package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTireAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTireAgreement("code-988", "label-301")
        val view = toManualTireAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTireAgreementViews(mutableListOf<ManualTireAgreement>(item, item)).size, "views")
    }
}
