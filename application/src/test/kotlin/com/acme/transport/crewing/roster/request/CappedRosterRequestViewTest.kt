package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRosterRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRosterRequest("code-79", "name-238")
        val view = toCappedRosterRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRosterRequestViews(mutableListOf<CappedRosterRequest>(item, item)).size, "views")
    }
}
