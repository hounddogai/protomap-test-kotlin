package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedBackhaulTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedBackhaulTarget("code-480", "label-855")
        val view = toBatchedBackhaulTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedBackhaulTargetViews(mutableListOf<BatchedBackhaulTarget>(item, item)).size, "views")
    }
}
