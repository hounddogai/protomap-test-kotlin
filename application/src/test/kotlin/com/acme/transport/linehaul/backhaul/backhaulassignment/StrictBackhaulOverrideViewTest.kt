package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictBackhaulOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictBackhaulOverride("code-249", "title-902")
        val view = toStrictBackhaulOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictBackhaulOverrideViews(mutableListOf<StrictBackhaulOverride>(item, item)).size, "views")
    }
}
