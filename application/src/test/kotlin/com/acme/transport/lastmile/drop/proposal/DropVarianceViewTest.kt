package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DropVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropVariance("code-877", "name-405")
        val view = toDropVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropVarianceViews(mutableListOf<DropVariance>(item, item)).size, "views")
    }
}
