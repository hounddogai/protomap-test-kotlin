package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryViolationTierTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryViolationTier("code-779", "label-715")
        assertEquals(PrimaryViolationTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newPrimaryViolationTier("code-779", "label-715")
        try {
            item.moveTo(PrimaryViolationTierStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryViolationTierStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryViolationTier("code-779", "label-715")
        item.status = PrimaryViolationTierStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryViolationTierStatus.REQUESTED) }
    }
}
