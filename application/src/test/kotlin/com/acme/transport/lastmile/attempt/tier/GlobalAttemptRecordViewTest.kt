package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAttemptRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAttemptRecord("code-926", "label-728")
        val view = toGlobalAttemptRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAttemptRecordViews(mutableListOf<GlobalAttemptRecord>(item, item)).size, "views")
    }
}
