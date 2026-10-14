package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRosterRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRosterRequest("code-811", "name-212")
        val view = toFallbackRosterRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRosterRequestViews(mutableListOf<FallbackRosterRequest>(item, item)).size, "views")
    }
}
