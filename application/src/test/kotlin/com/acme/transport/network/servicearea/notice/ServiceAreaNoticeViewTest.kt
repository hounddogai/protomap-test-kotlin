package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaNotice("code-699", "name-340")
        val view = toServiceAreaNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaNoticeViews(mutableListOf<ServiceAreaNotice>(item, item)).size, "views")
    }
}
