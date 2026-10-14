package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictServicePlanClaimTest {
    @Test
    fun `starts in open`() {
        val item = newStrictServicePlanClaim("code-443", "label-584")
        assertEquals(StrictServicePlanClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictServicePlanClaim("code-443", "label-584")
        try {
            item.moveTo(StrictServicePlanClaimStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictServicePlanClaimStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictServicePlanClaim("code-443", "label-584")
        item.status = StrictServicePlanClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictServicePlanClaimStatus.OPEN) }
    }
}
