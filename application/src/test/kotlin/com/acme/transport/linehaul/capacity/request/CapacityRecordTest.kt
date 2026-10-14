package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityRecordTest {
    @Test
    fun `starts in open`() {
        val item = newCapacityRecord("code-900", "name-683")
        assertEquals(CapacityRecordStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newCapacityRecord("code-900", "name-683")
        try {
            item.moveTo(CapacityRecordStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityRecordStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCapacityRecord("code-900", "name-683")
        item.status = CapacityRecordStatus.ARCHIVED
        assertFails("move") { item.moveTo(CapacityRecordStatus.OPEN) }
    }
}
