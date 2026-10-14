package com.acme.transport.network.cutoff.cutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffTarget("code-32", "name-642")
        val view = toCutoffTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffTargetViews(mutableListOf<CutoffTarget>(item, item)).size, "views")
    }
}
