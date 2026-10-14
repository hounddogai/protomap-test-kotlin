package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class NearMissBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNearMissBatch("code-713", "title-68")
        val view = toNearMissBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNearMissBatchViews(mutableListOf<NearMissBatch>(item, item)).size, "views")
    }
}
