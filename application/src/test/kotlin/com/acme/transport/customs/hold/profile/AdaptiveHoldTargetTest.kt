package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveHoldTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveHoldTarget("code-409", "name-70")
        assertEquals(AdaptiveHoldTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveHoldTarget("code-409", "name-70")
        try {
            item.moveTo(AdaptiveHoldTargetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveHoldTargetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveHoldTarget("code-409", "name-70")
        item.status = AdaptiveHoldTargetStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveHoldTargetStatus.DRAFT) }
    }
}
