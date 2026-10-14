package com.acme.transport.terminal.sortation.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class SortationCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSortationCheckpoint("code-229", "name-709")
        val view = toSortationCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSortationCheckpointViews(mutableListOf<SortationCheckpoint>(item, item)).size, "views")
    }
}
