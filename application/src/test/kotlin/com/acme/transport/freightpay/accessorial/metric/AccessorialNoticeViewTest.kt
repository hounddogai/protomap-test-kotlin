package com.acme.transport.freightpay.accessorial.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialNotice("code-421", "label-329")
        val view = toAccessorialNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialNoticeViews(mutableListOf<AccessorialNotice>(item, item)).size, "views")
    }
}
