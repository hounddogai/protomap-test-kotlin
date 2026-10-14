package com.acme.transport.network.cutoff.cutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalCutoffRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalCutoffRollup("code-18", "title-713")
        val view = toGlobalCutoffRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalCutoffRollupViews(mutableListOf<GlobalCutoffRollup>(item, item)).size, "views")
    }
}
