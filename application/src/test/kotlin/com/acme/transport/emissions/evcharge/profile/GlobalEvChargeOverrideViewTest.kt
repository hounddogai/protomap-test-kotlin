package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalEvChargeOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalEvChargeOverride("code-677", "name-98")
        val view = toGlobalEvChargeOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalEvChargeOverrideViews(mutableListOf<GlobalEvChargeOverride>(item, item)).size, "views")
    }
}
