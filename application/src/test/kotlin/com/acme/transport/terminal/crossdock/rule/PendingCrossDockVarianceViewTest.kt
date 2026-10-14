package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCrossDockVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCrossDockVariance("code-365", "name-782")
        val view = toPendingCrossDockVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCrossDockVarianceViews(mutableListOf<PendingCrossDockVariance>(item, item)).size, "views")
    }
}
