package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRosterRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRosterRecord("code-863", "name-187")
        val view = toPrimaryRosterRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRosterRecordViews(mutableListOf<PrimaryRosterRecord>(item, item)).size, "views")
    }
}
