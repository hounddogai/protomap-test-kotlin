package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityProfile("code-758", "name-772")
        assertEquals(CapacityProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCapacityProfile("code-758", "name-772")
        try {
            item.moveTo(CapacityProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityProfile("code-758", "name-772")
        item.status = CapacityProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityProfileStatus.REQUESTED) }
    }
}
