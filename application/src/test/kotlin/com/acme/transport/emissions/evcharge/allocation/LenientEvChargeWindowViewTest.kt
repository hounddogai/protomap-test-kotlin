package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientEvChargeWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientEvChargeWindow("code-848", "name-102")
        val view = toLenientEvChargeWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientEvChargeWindowViews(mutableListOf<LenientEvChargeWindow>(item, item)).size, "views")
    }
}
