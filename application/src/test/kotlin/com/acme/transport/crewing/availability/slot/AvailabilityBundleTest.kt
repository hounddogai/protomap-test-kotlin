package com.acme.transport.crewing.availability.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newAvailabilityBundle("code-887", "label-631")
        assertEquals(AvailabilityBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAvailabilityBundle("code-887", "label-631")
        try {
            item.moveTo(AvailabilityBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAvailabilityBundle("code-887", "label-631")
        item.status = AvailabilityBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AvailabilityBundleStatus.PROPOSED) }
    }
}
