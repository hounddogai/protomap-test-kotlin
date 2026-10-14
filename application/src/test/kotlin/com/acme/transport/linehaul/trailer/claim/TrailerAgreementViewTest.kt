package com.acme.transport.linehaul.trailer.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerAgreement("code-559", "name-990")
        val view = toTrailerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerAgreementViews(mutableListOf<TrailerAgreement>(item, item)).size, "views")
    }
}
