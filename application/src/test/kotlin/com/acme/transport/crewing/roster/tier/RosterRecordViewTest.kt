package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class RosterRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRosterRecord("code-948", "title-842")
        val view = toRosterRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRosterRecordViews(mutableListOf<RosterRecord>(item, item)).size, "views")
    }
}
