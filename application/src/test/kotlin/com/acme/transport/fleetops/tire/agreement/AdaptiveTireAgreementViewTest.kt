package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTireAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTireAgreement("code-196", "title-642")
        val view = toAdaptiveTireAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTireAgreementViews(mutableListOf<AdaptiveTireAgreement>(item, item)).size, "views")
    }
}
