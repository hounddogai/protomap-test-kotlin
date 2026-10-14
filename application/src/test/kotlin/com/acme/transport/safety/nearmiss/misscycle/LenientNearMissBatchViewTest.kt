package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientNearMissBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientNearMissBatch("code-518", "title-324")
        val view = toLenientNearMissBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientNearMissBatchViews(mutableListOf<LenientNearMissBatch>(item, item)).size, "views")
    }
}
