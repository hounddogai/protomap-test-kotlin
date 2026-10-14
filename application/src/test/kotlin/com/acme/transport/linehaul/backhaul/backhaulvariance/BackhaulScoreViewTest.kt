package com.acme.transport.linehaul.backhaul.backhaulvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulScore("code-531", "label-890")
        val view = toBackhaulScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulScoreViews(mutableListOf<BackhaulScore>(item, item)).size, "views")
    }
}
