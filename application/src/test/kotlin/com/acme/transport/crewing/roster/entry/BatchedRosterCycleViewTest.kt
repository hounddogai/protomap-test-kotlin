package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRosterCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRosterCycle("code-799", "label-572")
        val view = toBatchedRosterCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRosterCycleViews(mutableListOf<BatchedRosterCycle>(item, item)).size, "views")
    }
}
