package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardProfile("code-498", "label-9")
        val view = toScorecardProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardProfileViews(mutableListOf<ScorecardProfile>(item, item)).size, "views")
    }
}
