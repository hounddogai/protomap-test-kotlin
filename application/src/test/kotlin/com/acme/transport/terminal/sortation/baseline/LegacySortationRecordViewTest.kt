package com.acme.transport.terminal.sortation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySortationRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacySortationRecord("code-89", "label-271")
        val view = toLegacySortationRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacySortationRecordViews(mutableListOf<LegacySortationRecord>(item, item)).size, "views")
    }
}
