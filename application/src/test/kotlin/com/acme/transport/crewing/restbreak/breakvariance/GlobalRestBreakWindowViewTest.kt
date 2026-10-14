package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRestBreakWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRestBreakWindow("code-755", "title-474")
        val view = toGlobalRestBreakWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRestBreakWindowViews(mutableListOf<GlobalRestBreakWindow>(item, item)).size, "views")
    }
}
