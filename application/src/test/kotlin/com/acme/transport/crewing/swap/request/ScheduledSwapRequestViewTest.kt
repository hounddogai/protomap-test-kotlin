package com.acme.transport.crewing.swap.request

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledSwapRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledSwapRequest("code-530", "name-148")
        val view = toScheduledSwapRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledSwapRequestViews(mutableListOf<ScheduledSwapRequest>(item, item)).size, "views")
    }
}
