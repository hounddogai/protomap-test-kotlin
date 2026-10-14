package com.acme.transport.linehaul.backhaul.cappedbackhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingBackhaulScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingBackhaulScore("code-39", "title-55")
        val view = toPendingBackhaulScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingBackhaulScoreViews(mutableListOf<PendingBackhaulScore>(item, item)).size, "views")
    }
}
