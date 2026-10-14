package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireSlotTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireSlot("code-14", "label-820")
        assertEquals(TireSlotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTireSlot("code-14", "label-820")
        try {
            item.moveTo(TireSlotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireSlotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireSlot("code-14", "label-820")
        item.status = TireSlotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireSlotStatus.PROPOSED) }
    }
}
