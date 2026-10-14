package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientLeaseReturnManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        val view = toLenientLeaseReturnManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientLeaseReturnManifestViews(mutableListOf<LenientLeaseReturnManifest>(item, item)).size, "views")
    }
}
