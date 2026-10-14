package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffAgreement("code-854", "title-697")
        val view = toCutoffAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffAgreementViews(mutableListOf<CutoffAgreement>(item, item)).size, "views")
    }
}
