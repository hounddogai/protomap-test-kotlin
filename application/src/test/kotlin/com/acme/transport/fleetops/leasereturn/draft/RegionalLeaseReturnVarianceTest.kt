package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalLeaseReturnVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalLeaseReturnVariance("code-219", "title-417")
        assertEquals(RegionalLeaseReturnVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRegionalLeaseReturnVariance("code-219", "title-417")
        try {
            item.moveTo(RegionalLeaseReturnVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalLeaseReturnVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalLeaseReturnVariance("code-219", "title-417")
        item.status = RegionalLeaseReturnVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalLeaseReturnVarianceStatus.IDLE) }
    }
}
