package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardAgreement("code-705", "name-179")
        val view = toScorecardAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardAgreementViews(mutableListOf<ScorecardAgreement>(item, item)).size, "views")
    }
}
