package com.acme.transport.safety.coaching.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCoachingNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCoachingNotice("code-289", "title-76")
        val view = toManualCoachingNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCoachingNoticeViews(mutableListOf<ManualCoachingNotice>(item, item)).size, "views")
    }
}
