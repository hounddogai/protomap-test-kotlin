package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TireBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireBatch("code-404", "title-255")
        val view = toTireBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireBatchViews(mutableListOf<TireBatch>(item, item)).size, "views")
    }
}
