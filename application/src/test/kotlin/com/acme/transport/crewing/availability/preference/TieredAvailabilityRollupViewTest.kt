package com.acme.transport.crewing.availability.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAvailabilityRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAvailabilityRollup("code-710", "name-605")
        val view = toTieredAvailabilityRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAvailabilityRollupViews(mutableListOf<TieredAvailabilityRollup>(item, item)).size, "views")
    }
}
