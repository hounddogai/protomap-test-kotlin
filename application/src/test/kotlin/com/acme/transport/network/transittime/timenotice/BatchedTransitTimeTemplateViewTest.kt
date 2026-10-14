package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTransitTimeTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTransitTimeTemplate("code-420", "name-192")
        val view = toBatchedTransitTimeTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTransitTimeTemplateViews(mutableListOf<BatchedTransitTimeTemplate>(item, item)).size, "views")
    }
}
