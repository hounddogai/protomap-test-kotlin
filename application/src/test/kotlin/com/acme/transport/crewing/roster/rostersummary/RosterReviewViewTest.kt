package com.acme.transport.crewing.roster.rostersummary

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterReview("code-644", "name-48")
        val view = toRosterReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterReviewViews(mutableListOf<RosterReview>(item, item)).size, "views")
    }
}
