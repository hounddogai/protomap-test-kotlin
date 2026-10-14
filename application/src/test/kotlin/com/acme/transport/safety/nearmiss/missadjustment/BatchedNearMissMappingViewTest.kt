package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNearMissMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNearMissMapping("code-384", "name-350")
        val view = toBatchedNearMissMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNearMissMappingViews(mutableListOf<BatchedNearMissMapping>(item, item)).size, "views")
    }
}
