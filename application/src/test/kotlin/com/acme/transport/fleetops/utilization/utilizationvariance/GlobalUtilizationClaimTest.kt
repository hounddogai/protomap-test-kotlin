package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalUtilizationClaimTest {
    @Test
    fun `starts in new`() {
        val item = newGlobalUtilizationClaim("code-565", "title-951")
        assertEquals(GlobalUtilizationClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newGlobalUtilizationClaim("code-565", "title-951")
        try {
            item.moveTo(GlobalUtilizationClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalUtilizationClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newGlobalUtilizationClaim("code-565", "title-951")
        item.status = GlobalUtilizationClaimStatus.FAILED
        assertFails("move") { item.moveTo(GlobalUtilizationClaimStatus.NEW) }
    }
}
