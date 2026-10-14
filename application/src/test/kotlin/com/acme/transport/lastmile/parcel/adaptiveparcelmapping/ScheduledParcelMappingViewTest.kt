package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledParcelMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledParcelMapping("code-722", "title-672")
        val view = toScheduledParcelMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledParcelMappingViews(mutableListOf<ScheduledParcelMapping>(item, item)).size, "views")
    }
}
