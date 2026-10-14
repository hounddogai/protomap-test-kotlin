package com.acme.transport.linehaul.relay.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalRelayVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalRelayVariance("code-653", "label-136")
        val view = toGlobalRelayVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalRelayVarianceViews(mutableListOf<GlobalRelayVariance>(item, item)).size, "views")
    }
}
