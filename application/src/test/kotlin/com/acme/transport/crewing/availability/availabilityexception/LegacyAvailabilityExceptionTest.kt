package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAvailabilityExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyAvailabilityException("code-982", "name-315")
        assertEquals(LegacyAvailabilityExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyAvailabilityException("code-982", "name-315")
        try {
            item.moveTo(LegacyAvailabilityExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAvailabilityExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyAvailabilityException("code-982", "name-315")
        item.status = LegacyAvailabilityExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyAvailabilityExceptionStatus.REQUESTED) }
    }
}
