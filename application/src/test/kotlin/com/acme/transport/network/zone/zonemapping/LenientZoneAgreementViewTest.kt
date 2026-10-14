package com.acme.transport.network.zone.zonemapping

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientZoneAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientZoneAgreement("code-530", "label-18")
        val view = toLenientZoneAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientZoneAgreementViews(mutableListOf<LenientZoneAgreement>(item, item)).size, "views")
    }
}
