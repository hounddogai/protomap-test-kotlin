package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAccessorialScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAccessorialScore("code-498", "label-857")
        val view = toGlobalAccessorialScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAccessorialScoreViews(mutableListOf<GlobalAccessorialScore>(item, item)).size, "views")
    }
}
