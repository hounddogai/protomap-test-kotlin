package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRelayReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRelayReview("code-273", "name-408")
        val view = toCappedRelayReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRelayReviewViews(mutableListOf<CappedRelayReview>(item, item)).size, "views")
    }
}
