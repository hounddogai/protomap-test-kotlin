package com.acme.transport.lastmile.attempt.attemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAttemptRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAttemptRequest("code-141", "name-247")
        val view = toLenientAttemptRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAttemptRequestViews(mutableListOf<LenientAttemptRequest>(item, item)).size, "views")
    }
}
