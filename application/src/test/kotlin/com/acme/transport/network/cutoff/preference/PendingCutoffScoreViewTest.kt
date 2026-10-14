package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCutoffScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCutoffScore("code-969", "label-691")
        val view = toPendingCutoffScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCutoffScoreViews(mutableListOf<PendingCutoffScore>(item, item)).size, "views")
    }
}
