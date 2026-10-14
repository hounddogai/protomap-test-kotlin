package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedCoachingWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedCoachingWindow("code-354", "label-852")
        val view = toBatchedCoachingWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedCoachingWindowViews(mutableListOf<BatchedCoachingWindow>(item, item)).size, "views")
    }
}
