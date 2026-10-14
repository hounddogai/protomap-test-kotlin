package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newHoldTarget("code-530", "name-566")
        assertEquals(HoldTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newHoldTarget("code-530", "name-566")
        try {
            item.moveTo(HoldTargetStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldTargetStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHoldTarget("code-530", "name-566")
        item.status = HoldTargetStatus.CLOSED
        assertFails("move") { item.moveTo(HoldTargetStatus.DRAFT) }
    }
}
