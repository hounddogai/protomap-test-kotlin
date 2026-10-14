package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TireProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireProfile("code-671", "name-614")
        val view = toTireProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireProfileViews(mutableListOf<TireProfile>(item, item)).size, "views")
    }
}
