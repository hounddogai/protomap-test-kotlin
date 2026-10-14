package com.acme.transport.fleetops.utilization.utilizationsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedUtilizationClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedUtilizationClaim("code-885", "title-779")
        assertEquals(CappedUtilizationClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newCappedUtilizationClaim("code-885", "title-779")
        try {
            item.moveTo(CappedUtilizationClaimStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedUtilizationClaimStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedUtilizationClaim("code-885", "title-779")
        item.status = CappedUtilizationClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedUtilizationClaimStatus.REQUESTED) }
    }
}
