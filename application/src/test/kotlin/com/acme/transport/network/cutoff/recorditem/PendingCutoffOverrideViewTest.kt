package com.acme.transport.network.cutoff.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCutoffOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCutoffOverride("code-863", "title-881")
        val view = toPendingCutoffOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCutoffOverrideViews(mutableListOf<PendingCutoffOverride>(item, item)).size, "views")
    }
}
