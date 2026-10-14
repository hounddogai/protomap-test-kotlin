package com.acme.transport.fleetops.utilization.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newManualUtilizationClaim("code-692", "label-119")
        assertEquals(ManualUtilizationClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualUtilizationClaim("code-692", "label-119")
        try {
            item.moveTo(ManualUtilizationClaimStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationClaimStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualUtilizationClaim("code-692", "label-119")
        item.status = ManualUtilizationClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualUtilizationClaimStatus.REQUESTED) }
    }
}
