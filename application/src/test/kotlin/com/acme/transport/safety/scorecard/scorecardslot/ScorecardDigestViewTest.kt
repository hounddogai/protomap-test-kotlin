package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals

class ScorecardDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScorecardDigest("code-829", "label-947")
        val view = toScorecardDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScorecardDigestViews(mutableListOf<ScorecardDigest>(item, item)).size, "views")
    }
}
