package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityEntry("code-531", "title-115")
        assertEquals(CapacityEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCapacityEntry("code-531", "title-115")
        try {
            item.moveTo(CapacityEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityEntry("code-531", "title-115")
        item.status = CapacityEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityEntryStatus.REQUESTED) }
    }
}
