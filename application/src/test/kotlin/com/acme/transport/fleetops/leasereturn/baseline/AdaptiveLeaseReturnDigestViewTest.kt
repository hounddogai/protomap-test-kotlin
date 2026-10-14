package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLeaseReturnDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        val view = toAdaptiveLeaseReturnDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveLeaseReturnDigestViews(mutableListOf<AdaptiveLeaseReturnDigest>(item, item)).size, "views")
    }
}
