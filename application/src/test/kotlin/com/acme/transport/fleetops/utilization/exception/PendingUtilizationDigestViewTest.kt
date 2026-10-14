package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingUtilizationDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingUtilizationDigest("code-976", "label-739")
        val view = toPendingUtilizationDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingUtilizationDigestViews(mutableListOf<PendingUtilizationDigest>(item, item)).size, "views")
    }
}
