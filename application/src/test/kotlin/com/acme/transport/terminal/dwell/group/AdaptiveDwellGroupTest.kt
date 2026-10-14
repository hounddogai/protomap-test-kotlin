package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDwellGroupTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveDwellGroup("code-869", "name-433")
        assertEquals(AdaptiveDwellGroupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveDwellGroup("code-869", "name-433")
        try {
            item.moveTo(AdaptiveDwellGroupStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDwellGroupStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveDwellGroup("code-869", "name-433")
        item.status = AdaptiveDwellGroupStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveDwellGroupStatus.DRAFT) }
    }
}
