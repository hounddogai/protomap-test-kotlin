package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictLeaseReturnBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictLeaseReturnBaseline("code-901", "label-59")
        assertEquals(StrictLeaseReturnBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newStrictLeaseReturnBaseline("code-901", "label-59")
        try {
            item.moveTo(StrictLeaseReturnBaselineStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictLeaseReturnBaselineStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictLeaseReturnBaseline("code-901", "label-59")
        item.status = StrictLeaseReturnBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictLeaseReturnBaselineStatus.PROPOSED) }
    }
}
