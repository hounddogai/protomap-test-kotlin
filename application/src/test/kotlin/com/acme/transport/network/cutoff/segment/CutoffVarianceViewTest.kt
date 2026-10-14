package com.acme.transport.network.cutoff.segment

import kotlin.test.Test
import kotlin.test.assertEquals

class CutoffVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCutoffVariance("code-364", "name-394")
        val view = toCutoffVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCutoffVarianceViews(mutableListOf<CutoffVariance>(item, item)).size, "views")
    }
}
