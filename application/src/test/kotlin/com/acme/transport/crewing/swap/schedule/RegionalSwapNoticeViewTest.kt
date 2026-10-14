package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalSwapNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalSwapNotice("code-93", "label-864")
        val view = toRegionalSwapNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalSwapNoticeViews(mutableListOf<RegionalSwapNotice>(item, item)).size, "views")
    }
}
