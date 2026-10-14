package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityTarget("code-387", "label-373")
        assertEquals(CapacityTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCapacityTarget("code-387", "label-373")
        try {
            item.moveTo(CapacityTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityTarget("code-387", "label-373")
        item.status = CapacityTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityTargetStatus.PROPOSED) }
    }
}
