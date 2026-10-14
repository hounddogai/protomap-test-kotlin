package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAccessorialScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAccessorialScore("code-18", "label-482")
        val view = toFallbackAccessorialScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAccessorialScoreViews(mutableListOf<FallbackAccessorialScore>(item, item)).size, "views")
    }
}
