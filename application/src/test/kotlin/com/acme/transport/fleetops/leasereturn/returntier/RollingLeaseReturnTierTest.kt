package com.acme.transport.fleetops.leasereturn.returntier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingLeaseReturnTierTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingLeaseReturnTier("code-542", "name-980")
        assertEquals(RollingLeaseReturnTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingLeaseReturnTier("code-542", "name-980")
        try {
            item.moveTo(RollingLeaseReturnTierStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingLeaseReturnTierStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingLeaseReturnTier("code-542", "name-980")
        item.status = RollingLeaseReturnTierStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingLeaseReturnTierStatus.PLANNED) }
    }
}
