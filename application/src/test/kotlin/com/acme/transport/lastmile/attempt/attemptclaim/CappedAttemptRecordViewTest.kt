package com.acme.transport.lastmile.attempt.attemptclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedAttemptRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedAttemptRecord("code-728", "name-231")
        val view = toCappedAttemptRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedAttemptRecordViews(mutableListOf<CappedAttemptRecord>(item, item)).size, "views")
    }
}
