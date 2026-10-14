package com.acme.transport.fleetops.leasereturn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LeaseReturnVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newLeaseReturnVariance("code-631", "name-674")
        assertEquals(LeaseReturnVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newLeaseReturnVariance("code-631", "name-674")
        try {
            item.moveTo(LeaseReturnVarianceStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LeaseReturnVarianceStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLeaseReturnVariance("code-631", "name-674")
        item.status = LeaseReturnVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(LeaseReturnVarianceStatus.CREATED) }
    }
}
