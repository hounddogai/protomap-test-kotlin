package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAttemptRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAttemptRequest("code-140", "label-438")
        val view = toPendingAttemptRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAttemptRequestViews(mutableListOf<PendingAttemptRequest>(item, item)).size, "views")
    }
}
