package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAttemptRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAttemptRequest("code-934", "label-85")
        val view = toFallbackAttemptRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAttemptRequestViews(mutableListOf<FallbackAttemptRequest>(item, item)).size, "views")
    }
}
