package com.acme.transport.lastmile.parcel.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingParcelOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingParcelOverride("code-210", "name-729")
        val view = toPendingParcelOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingParcelOverrideViews(mutableListOf<PendingParcelOverride>(item, item)).size, "views")
    }
}
