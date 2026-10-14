package com.acme.transport.linehaul.capacity.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newCapacityScore("code-779", "label-460")
        assertEquals(CapacityScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCapacityScore("code-779", "label-460")
        try {
            item.moveTo(CapacityScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCapacityScore("code-779", "label-460")
        item.status = CapacityScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CapacityScoreStatus.PROPOSED) }
    }
}
