package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyDisputeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyDisputeTemplate("code-553", "label-755")
        val view = toLegacyDisputeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyDisputeTemplateViews(mutableListOf<LegacyDisputeTemplate>(item, item)).size, "views")
    }
}
