package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class ParcelAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newParcelAllocation("code-312", "name-529")
        val view = toParcelAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toParcelAllocationViews(mutableListOf<ParcelAllocation>(item, item)).size, "views")
    }
}
