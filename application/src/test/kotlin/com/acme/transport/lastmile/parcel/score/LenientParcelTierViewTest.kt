package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientParcelTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientParcelTier("code-991", "title-278")
        val view = toLenientParcelTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientParcelTierViews(mutableListOf<LenientParcelTier>(item, item)).size, "views")
    }
}
