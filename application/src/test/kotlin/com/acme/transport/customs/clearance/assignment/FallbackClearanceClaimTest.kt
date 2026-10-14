package com.acme.transport.customs.clearance.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackClearanceClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackClearanceClaim("code-67", "label-934")
        assertEquals(FallbackClearanceClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFallbackClearanceClaim("code-67", "label-934")
        try {
            item.moveTo(FallbackClearanceClaimStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackClearanceClaimStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackClearanceClaim("code-67", "label-934")
        item.status = FallbackClearanceClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackClearanceClaimStatus.REQUESTED) }
    }
}
