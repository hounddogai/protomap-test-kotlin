package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCrossDockClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        val view = toWeightedCrossDockClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCrossDockClaimViews(mutableListOf<WeightedCrossDockClaim>(item, item)).size, "views")
    }
}
