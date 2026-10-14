package com.acme.transport.lastmile.attempt.adaptiveattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveAttemptRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveAttemptRequest("code-854", "label-82")
        val view = toAdaptiveAttemptRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveAttemptRequestViews(mutableListOf<AdaptiveAttemptRequest>(item, item)).size, "views")
    }
}
