package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCutoffRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCutoffRollup("code-354", "label-997")
        val view = toStrictCutoffRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCutoffRollupViews(mutableListOf<StrictCutoffRollup>(item, item)).size, "views")
    }
}
