package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newUtilizationClaim("code-750", "name-193")
        assertEquals(UtilizationClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newUtilizationClaim("code-750", "name-193")
        try {
            item.moveTo(UtilizationClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newUtilizationClaim("code-750", "name-193")
        item.status = UtilizationClaimStatus.STOPPED
        assertFails("move") { item.moveTo(UtilizationClaimStatus.IDLE) }
    }
}
