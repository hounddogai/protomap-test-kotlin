package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedClearanceBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedClearanceBundle("code-297", "label-514")
        val view = toCappedClearanceBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedClearanceBundleViews(mutableListOf<CappedClearanceBundle>(item, item)).size, "views")
    }
}
