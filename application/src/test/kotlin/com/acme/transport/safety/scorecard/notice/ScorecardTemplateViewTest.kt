package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardTemplate("code-239", "title-998")
        val view = toScorecardTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardTemplateViews(mutableListOf<ScorecardTemplate>(item, item)).size, "views")
    }
}
