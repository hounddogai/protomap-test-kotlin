package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityMappingTest {
    @Test
    fun `starts in new`() {
        val item = newAvailabilityMapping("code-871", "name-971")
        assertEquals(AvailabilityMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAvailabilityMapping("code-871", "name-971")
        try {
            item.moveTo(AvailabilityMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAvailabilityMapping("code-871", "name-971")
        item.status = AvailabilityMappingStatus.FAILED
        assertFails("move") { item.moveTo(AvailabilityMappingStatus.NEW) }
    }
}
