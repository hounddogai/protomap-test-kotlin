package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNearMissPolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNearMissPolicy("code-358", "name-78")
        val view = toLenientNearMissPolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNearMissPolicyViews(mutableListOf<LenientNearMissPolicy>(item, item)).size, "views")
    }
}
