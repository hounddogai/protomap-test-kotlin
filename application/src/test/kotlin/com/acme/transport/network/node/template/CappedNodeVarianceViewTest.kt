package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedNodeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedNodeVariance("code-532", "name-670")
        val view = toCappedNodeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedNodeVarianceViews(mutableListOf<CappedNodeVariance>(item, item)).size, "views")
    }
}
