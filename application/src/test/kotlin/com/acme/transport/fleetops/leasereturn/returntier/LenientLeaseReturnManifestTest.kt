package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientLeaseReturnManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        assertEquals(LenientLeaseReturnManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        try {
            item.moveTo(LenientLeaseReturnManifestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientLeaseReturnManifestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLenientLeaseReturnManifest("code-553", "title-540")
        item.status = LenientLeaseReturnManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(LenientLeaseReturnManifestStatus.PLANNED) }
    }
}
