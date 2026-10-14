package com.acme.transport.safety.nearmiss.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNearMissMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNearMissMapping("code-835", "name-239")
        val view = toLenientNearMissMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNearMissMappingViews(mutableListOf<LenientNearMissMapping>(item, item)).size, "views")
    }
}
