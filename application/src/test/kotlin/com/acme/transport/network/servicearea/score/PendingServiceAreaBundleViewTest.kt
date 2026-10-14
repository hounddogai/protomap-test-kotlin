package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingServiceAreaBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingServiceAreaBundle("code-382", "name-893")
        val view = toPendingServiceAreaBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingServiceAreaBundleViews(mutableListOf<PendingServiceAreaBundle>(item, item)).size, "views")
    }
}
