package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationRecord("code-490", "label-354")
        val view = toSortationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationRecordViews(mutableListOf<SortationRecord>(item, item)).size, "views")
    }
}
