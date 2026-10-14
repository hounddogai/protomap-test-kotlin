package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTenderAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        val view = toAdaptiveTenderAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTenderAgreementViews(mutableListOf<AdaptiveTenderAgreement>(item, item)).size, "views")
    }
}
