package com.acme.transport.network.servicearea.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServiceAreaEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServiceAreaEntry("code-146", "title-22")
        val view = toPendingServiceAreaEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServiceAreaEntryViews(mutableListOf<PendingServiceAreaEntry>(item, item)).size, "views")
    }
}
