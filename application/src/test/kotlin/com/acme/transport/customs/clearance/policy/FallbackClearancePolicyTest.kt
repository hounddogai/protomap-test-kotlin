package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackClearancePolicyTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackClearancePolicy("code-794", "title-993")
        assertEquals(FallbackClearancePolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newFallbackClearancePolicy("code-794", "title-993")
        try {
            item.moveTo(FallbackClearancePolicyStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackClearancePolicyStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackClearancePolicy("code-794", "title-993")
        item.status = FallbackClearancePolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackClearancePolicyStatus.OPEN) }
    }
}
