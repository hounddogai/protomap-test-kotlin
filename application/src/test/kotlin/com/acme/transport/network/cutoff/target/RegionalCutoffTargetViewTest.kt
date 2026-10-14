package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCutoffTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCutoffTarget("code-381", "name-62")
        val view = toRegionalCutoffTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCutoffTargetViews(mutableListOf<RegionalCutoffTarget>(item, item)).size, "views")
    }
}
