package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIncidentClaimTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveIncidentClaim("code-567", "name-521")
        assertEquals(AdaptiveIncidentClaimStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveIncidentClaim("code-567", "name-521")
        try {
            item.moveTo(AdaptiveIncidentClaimStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIncidentClaimStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveIncidentClaim("code-567", "name-521")
        item.status = AdaptiveIncidentClaimStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveIncidentClaimStatus.IDLE) }
    }
}
