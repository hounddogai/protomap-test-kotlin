package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityScore("code-695", "title-883")
        val view = toAvailabilityScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityScoreViews(mutableListOf<AvailabilityScore>(item, item)).size, "views")
    }
}
