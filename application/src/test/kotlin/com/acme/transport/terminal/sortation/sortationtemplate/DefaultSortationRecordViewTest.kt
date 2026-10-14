package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultSortationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultSortationRecord("code-341", "label-438")
        val view = toDefaultSortationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultSortationRecordViews(mutableListOf<DefaultSortationRecord>(item, item)).size, "views")
    }
}
