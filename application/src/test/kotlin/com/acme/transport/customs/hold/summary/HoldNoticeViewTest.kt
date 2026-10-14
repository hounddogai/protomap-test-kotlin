package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldNotice("code-126", "title-940")
        val view = toHoldNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldNoticeViews(mutableListOf<HoldNotice>(item, item)).size, "views")
    }
}
