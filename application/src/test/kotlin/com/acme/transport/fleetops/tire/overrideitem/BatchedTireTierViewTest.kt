package com.acme.transport.fleetops.tire.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTireTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTireTier("code-163", "name-36")
        val view = toBatchedTireTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTireTierViews(mutableListOf<BatchedTireTier>(item, item)).size, "views")
    }
}
