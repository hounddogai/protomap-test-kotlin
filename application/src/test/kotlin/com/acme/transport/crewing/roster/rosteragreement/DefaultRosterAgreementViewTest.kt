package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultRosterAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultRosterAgreement("code-404", "label-177")
        val view = toDefaultRosterAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultRosterAgreementViews(mutableListOf<DefaultRosterAgreement>(item, item)).size, "views")
    }
}
