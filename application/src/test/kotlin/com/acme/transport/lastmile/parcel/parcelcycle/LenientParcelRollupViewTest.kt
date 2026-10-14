package com.acme.transport.lastmile.parcel.parcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientParcelRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientParcelRollup("code-358", "name-596")
        val view = toLenientParcelRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientParcelRollupViews(mutableListOf<LenientParcelRollup>(item, item)).size, "views")
    }
}
