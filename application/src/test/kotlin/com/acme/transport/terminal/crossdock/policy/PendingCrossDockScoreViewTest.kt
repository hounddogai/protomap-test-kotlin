package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCrossDockScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCrossDockScore("code-42", "title-107")
        val view = toPendingCrossDockScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCrossDockScoreViews(mutableListOf<PendingCrossDockScore>(item, item)).size, "views")
    }
}
