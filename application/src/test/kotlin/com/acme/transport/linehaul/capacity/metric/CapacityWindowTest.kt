package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newCapacityWindow("code-895", "name-832")
        assertEquals(CapacityWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCapacityWindow("code-895", "name-832")
        try {
            item.moveTo(CapacityWindowStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityWindowStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCapacityWindow("code-895", "name-832")
        item.status = CapacityWindowStatus.CLOSED
        assertFails("move") { item.moveTo(CapacityWindowStatus.DRAFT) }
    }
}
