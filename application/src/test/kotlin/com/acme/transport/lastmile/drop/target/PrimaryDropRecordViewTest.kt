package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDropRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDropRecord("code-467", "name-666")
        val view = toPrimaryDropRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDropRecordViews(mutableListOf<PrimaryDropRecord>(item, item)).size, "views")
    }
}
