package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictIncidentClaimTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictIncidentClaim("code-25", "title-190")
        assertEquals(StrictIncidentClaimStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictIncidentClaim("code-25", "title-190")
        try {
            item.moveTo(StrictIncidentClaimStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictIncidentClaimStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictIncidentClaim("code-25", "title-190")
        item.status = StrictIncidentClaimStatus.CLOSED
        assertFails("move") { item.moveTo(StrictIncidentClaimStatus.DRAFT) }
    }
}
