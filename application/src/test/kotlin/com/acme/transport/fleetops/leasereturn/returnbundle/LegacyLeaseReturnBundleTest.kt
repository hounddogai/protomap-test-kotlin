package com.acme.transport.fleetops.leasereturn.returnbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLeaseReturnBundleTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyLeaseReturnBundle("code-53", "name-317")
        assertEquals(LegacyLeaseReturnBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyLeaseReturnBundle("code-53", "name-317")
        try {
            item.moveTo(LegacyLeaseReturnBundleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLeaseReturnBundleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyLeaseReturnBundle("code-53", "name-317")
        item.status = LegacyLeaseReturnBundleStatus.FAILED
        assertFails("move") { item.moveTo(LegacyLeaseReturnBundleStatus.NEW) }
    }
}
