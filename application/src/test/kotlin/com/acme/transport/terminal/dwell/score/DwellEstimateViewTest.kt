package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellEstimate("code-266", "title-320")
        val view = toDwellEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellEstimateViews(mutableListOf<DwellEstimate>(item, item)).size, "views")
    }
}
