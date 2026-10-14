package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationDigest("code-505", "title-503")
        val view = toUtilizationDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationDigestViews(mutableListOf<UtilizationDigest>(item, item)).size, "views")
    }
}
