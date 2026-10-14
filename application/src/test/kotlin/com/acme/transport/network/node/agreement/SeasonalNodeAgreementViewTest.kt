package com.acme.transport.network.node.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalNodeAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalNodeAgreement("code-534", "label-371")
        val view = toSeasonalNodeAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalNodeAgreementViews(mutableListOf<SeasonalNodeAgreement>(item, item)).size, "views")
    }
}
