package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationTierTest {
    @Test
    fun `starts in requested`() {
        val item = newViolationTier("code-505", "title-101")
        assertEquals(ViolationTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newViolationTier("code-505", "title-101")
        try {
            item.moveTo(ViolationTierStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationTierStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newViolationTier("code-505", "title-101")
        item.status = ViolationTierStatus.EXPIRED
        assertFails("move") { item.moveTo(ViolationTierStatus.REQUESTED) }
    }
}
