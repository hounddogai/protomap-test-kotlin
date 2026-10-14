package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class IdlingNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIdlingNotice("code-715", "title-194")
        val view = toIdlingNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIdlingNoticeViews(mutableListOf<IdlingNotice>(item, item)).size, "views")
    }
}
