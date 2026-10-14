package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterMapping("code-166", "label-664")
        val view = toRosterMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterMappingViews(mutableListOf<RosterMapping>(item, item)).size, "views")
    }
}
