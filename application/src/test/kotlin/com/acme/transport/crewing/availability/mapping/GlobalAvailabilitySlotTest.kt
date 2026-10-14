package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalAvailabilitySlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalAvailabilitySlot("code-185", "name-364")
        assertEquals(GlobalAvailabilitySlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalAvailabilitySlot("code-185", "name-364")
        try {
            item.moveTo(GlobalAvailabilitySlotStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalAvailabilitySlotStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalAvailabilitySlot("code-185", "name-364")
        item.status = GlobalAvailabilitySlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalAvailabilitySlotStatus.PROPOSED) }
    }
}
