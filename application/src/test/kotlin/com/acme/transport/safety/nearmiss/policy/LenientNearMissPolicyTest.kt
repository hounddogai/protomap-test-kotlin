package com.acme.transport.safety.nearmiss.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientNearMissPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientNearMissPolicy("code-358", "name-78")
        assertEquals(LenientNearMissPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientNearMissPolicy("code-358", "name-78")
        try {
            item.moveTo(LenientNearMissPolicyStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientNearMissPolicyStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientNearMissPolicy("code-358", "name-78")
        item.status = LenientNearMissPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(LenientNearMissPolicyStatus.DRAFT) }
    }
}
