package com.acme.transport.fleetops.leasereturn.returnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnManifestTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalLeaseReturnManifest("code-531", "label-18")
        assertEquals(RegionalLeaseReturnManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalLeaseReturnManifest("code-531", "label-18")
        try {
            item.moveTo(RegionalLeaseReturnManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalLeaseReturnManifest("code-531", "label-18")
        item.status = RegionalLeaseReturnManifestStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalLeaseReturnManifestStatus.CREATED) }
    }
}
