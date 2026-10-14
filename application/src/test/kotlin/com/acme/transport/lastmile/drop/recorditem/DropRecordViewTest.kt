package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class DropRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropRecord("code-432", "label-916")
        val view = toDropRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropRecordViews(mutableListOf<DropRecord>(item, item)).size, "views")
    }
}
