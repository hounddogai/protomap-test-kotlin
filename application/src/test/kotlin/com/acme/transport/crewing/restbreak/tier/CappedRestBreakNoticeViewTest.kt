package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRestBreakNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRestBreakNotice("code-732", "name-139")
        val view = toCappedRestBreakNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRestBreakNoticeViews(mutableListOf<CappedRestBreakNotice>(item, item)).size, "views")
    }
}
