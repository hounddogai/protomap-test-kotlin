package com.acme.transport.lastmile.parcel.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientParcelPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientParcelPolicy("code-460", "name-462")
        val view = toLenientParcelPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientParcelPolicyViews(mutableListOf<LenientParcelPolicy>(item, item)).size, "views")
    }
}
