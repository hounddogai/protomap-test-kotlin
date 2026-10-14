package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityPolicy("code-123", "name-357")
        assertEquals(CapacityPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCapacityPolicy("code-123", "name-357")
        try {
            item.moveTo(CapacityPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityPolicy("code-123", "name-357")
        item.status = CapacityPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityPolicyStatus.REQUESTED) }
    }
}
