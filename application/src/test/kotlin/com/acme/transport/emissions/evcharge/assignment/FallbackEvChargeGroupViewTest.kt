package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEvChargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEvChargeGroup("code-660", "title-67")
        val view = toFallbackEvChargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEvChargeGroupViews(mutableListOf<FallbackEvChargeGroup>(item, item)).size, "views")
    }
}
