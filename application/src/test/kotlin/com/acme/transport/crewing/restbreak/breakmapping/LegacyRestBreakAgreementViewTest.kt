package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRestBreakAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRestBreakAgreement("code-13", "label-980")
        val view = toLegacyRestBreakAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRestBreakAgreementViews(mutableListOf<LegacyRestBreakAgreement>(item, item)).size, "views")
    }
}
