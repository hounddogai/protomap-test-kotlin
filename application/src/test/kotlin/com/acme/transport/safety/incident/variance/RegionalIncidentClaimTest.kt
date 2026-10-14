package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalIncidentClaimTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalIncidentClaim("code-519", "name-624")
        assertEquals(RegionalIncidentClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalIncidentClaim("code-519", "name-624")
        try {
            item.moveTo(RegionalIncidentClaimStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalIncidentClaimStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalIncidentClaim("code-519", "name-624")
        item.status = RegionalIncidentClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalIncidentClaimStatus.OPEN) }
    }
}
