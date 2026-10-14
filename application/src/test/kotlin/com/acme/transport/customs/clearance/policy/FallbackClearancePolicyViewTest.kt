package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackClearancePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackClearancePolicy("code-794", "title-993")
        val view = toFallbackClearancePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackClearancePolicyViews(mutableListOf<FallbackClearancePolicy>(item, item)).size, "views")
    }
}
