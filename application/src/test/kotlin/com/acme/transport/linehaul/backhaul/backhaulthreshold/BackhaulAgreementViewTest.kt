package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulAgreement("code-944", "label-283")
        val view = toBackhaulAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulAgreementViews(mutableListOf<BackhaulAgreement>(item, item)).size, "views")
    }
}
