package com.acme.transport.customs.clearance.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceBatch("code-265", "name-933")
        val view = toClearanceBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceBatchViews(mutableListOf<ClearanceBatch>(item, item)).size, "views")
    }
}
