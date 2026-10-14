package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulEstimate("code-743", "name-483")
        val view = toBackhaulEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulEstimateViews(mutableListOf<BackhaulEstimate>(item, item)).size, "views")
    }
}
