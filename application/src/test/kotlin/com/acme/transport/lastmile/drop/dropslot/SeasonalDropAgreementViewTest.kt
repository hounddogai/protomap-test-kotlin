package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalDropAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalDropAgreement("code-430", "label-212")
        val view = toSeasonalDropAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalDropAgreementViews(mutableListOf<SeasonalDropAgreement>(item, item)).size, "views")
    }
}
