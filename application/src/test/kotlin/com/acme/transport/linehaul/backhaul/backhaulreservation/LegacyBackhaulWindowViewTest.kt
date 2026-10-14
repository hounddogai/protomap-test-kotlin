package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyBackhaulWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        val view = toLegacyBackhaulWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyBackhaulWindowViews(mutableListOf<LegacyBackhaulWindow>(item, item)).size, "views")
    }
}
