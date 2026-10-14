package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualParcelMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualParcelMapping("code-143", "name-543")
        val view = toManualParcelMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualParcelMappingViews(mutableListOf<ManualParcelMapping>(item, item)).size, "views")
    }
}
