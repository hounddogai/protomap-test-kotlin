package com.acme.transport.emissions.evcharge.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeProfile("code-924", "name-756")
        val view = toEvChargeProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeProfileViews(mutableListOf<EvChargeProfile>(item, item)).size, "views")
    }
}
