package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAttemptEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAttemptEntry("code-642", "name-548")
        val view = toPendingAttemptEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAttemptEntryViews(mutableListOf<PendingAttemptEntry>(item, item)).size, "views")
    }
}
