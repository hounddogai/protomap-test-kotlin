package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRosterReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRosterReview("code-335", "name-788")
        val view = toPrimaryRosterReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRosterReviewViews(mutableListOf<PrimaryRosterReview>(item, item)).size, "views")
    }
}
