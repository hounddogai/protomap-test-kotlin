package com.acme.transport.fleetops.tire.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class TireGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireGroup("code-552", "name-917")
        val view = toTireGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireGroupViews(mutableListOf<TireGroup>(item, item)).size, "views")
    }
}
