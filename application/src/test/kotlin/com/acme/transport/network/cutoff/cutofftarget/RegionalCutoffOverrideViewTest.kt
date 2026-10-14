package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalCutoffOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalCutoffOverride("code-828", "label-496")
        val view = toRegionalCutoffOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalCutoffOverrideViews(mutableListOf<RegionalCutoffOverride>(item, item)).size, "views")
    }
}
