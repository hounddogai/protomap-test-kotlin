package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapNotice("code-419", "title-511")
        val view = toSwapNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapNoticeViews(mutableListOf<SwapNotice>(item, item)).size, "views")
    }
}
