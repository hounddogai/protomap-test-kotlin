package com.acme.transport.network.transittime.timerequest

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTransitTimeRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTransitTimeRequest("code-604", "title-272")
        val view = toLenientTransitTimeRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTransitTimeRequestViews(mutableListOf<LenientTransitTimeRequest>(item, item)).size, "views")
    }
}
