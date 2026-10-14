package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelGroup("code-838", "name-592")
        val view = toParcelGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelGroupViews(mutableListOf<ParcelGroup>(item, item)).size, "views")
    }
}
