package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityRecord("code-936", "title-949")
        assertEquals(AvailabilityRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newAvailabilityRecord("code-936", "title-949")
        try {
            item.moveTo(AvailabilityRecordStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityRecordStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityRecord("code-936", "title-949")
        item.status = AvailabilityRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityRecordStatus.REQUESTED) }
    }
}
