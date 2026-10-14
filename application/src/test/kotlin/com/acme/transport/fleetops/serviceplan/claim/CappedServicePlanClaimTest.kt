package com.acme.transport.fleetops.serviceplan.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServicePlanClaimTest {
    @Test
    fun `starts in new`() {
        val item = newCappedServicePlanClaim("code-615", "title-743")
        assertEquals(CappedServicePlanClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newCappedServicePlanClaim("code-615", "title-743")
        try {
            item.moveTo(CappedServicePlanClaimStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServicePlanClaimStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedServicePlanClaim("code-615", "title-743")
        item.status = CappedServicePlanClaimStatus.FAILED
        assertFails("move") { item.moveTo(CappedServicePlanClaimStatus.NEW) }
    }
}
