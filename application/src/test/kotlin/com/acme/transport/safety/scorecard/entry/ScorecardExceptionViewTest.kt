package com.acme.transport.safety.scorecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardException("code-729", "title-539")
        val view = toScorecardExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardExceptionViews(mutableListOf<ScorecardException>(item, item)).size, "views")
    }
}
