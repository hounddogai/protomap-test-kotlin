package com.acme.transport.emissions.idling.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingBatch("code-820", "title-318")
        val view = toIdlingBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingBatchViews(mutableListOf<IdlingBatch>(item, item)).size, "views")
    }
}
