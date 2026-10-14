package com.acme.transport.lastmile.attempt.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAttemptRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAttemptRecord("code-679", "name-234")
        val view = toStrictAttemptRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAttemptRecordViews(mutableListOf<StrictAttemptRecord>(item, item)).size, "views")
    }
}
