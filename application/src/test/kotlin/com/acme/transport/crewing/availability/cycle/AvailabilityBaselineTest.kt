package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newAvailabilityBaseline("code-343", "title-939")
        assertEquals(AvailabilityBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newAvailabilityBaseline("code-343", "title-939")
        try {
            item.moveTo(AvailabilityBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAvailabilityBaseline("code-343", "title-939")
        item.status = AvailabilityBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(AvailabilityBaselineStatus.REQUESTED) }
    }
}
