package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class AttemptNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAttemptNotice("code-387", "label-891")
        val view = toAttemptNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAttemptNoticeViews(mutableListOf<AttemptNotice>(item, item)).size, "views")
    }
}
