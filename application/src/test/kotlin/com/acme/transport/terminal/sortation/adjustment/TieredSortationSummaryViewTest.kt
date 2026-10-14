package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredSortationSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredSortationSummary("code-26", "label-887")
        val view = toTieredSortationSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredSortationSummaryViews(mutableListOf<TieredSortationSummary>(item, item)).size, "views")
    }
}
