package com.acme.transport.fleetops.leasereturn.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newLeaseReturnWindow("code-980", "label-103")
        assertEquals(LeaseReturnWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLeaseReturnWindow("code-980", "label-103")
        try {
            item.moveTo(LeaseReturnWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLeaseReturnWindow("code-980", "label-103")
        item.status = LeaseReturnWindowStatus.CLOSED
        assertFails("move") { item.moveTo(LeaseReturnWindowStatus.DRAFT) }
    }
}
