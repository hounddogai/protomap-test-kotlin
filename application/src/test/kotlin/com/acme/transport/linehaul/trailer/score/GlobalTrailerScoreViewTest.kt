package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalTrailerScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalTrailerScore("code-951", "name-580")
        val view = toGlobalTrailerScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalTrailerScoreViews(mutableListOf<GlobalTrailerScore>(item, item)).size, "views")
    }
}
