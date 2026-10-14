package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientParcelMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientParcelMapping("code-651", "name-519")
        val view = toLenientParcelMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientParcelMappingViews(mutableListOf<LenientParcelMapping>(item, item)).size, "views")
    }
}
