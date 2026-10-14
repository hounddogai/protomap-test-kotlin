package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCameraEventCycleTest {
    @Test
    fun `starts in draft`() {
        val item = newStrictCameraEventCycle("code-520", "title-720")
        assertEquals(StrictCameraEventCycleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newStrictCameraEventCycle("code-520", "title-720")
        try {
            item.moveTo(StrictCameraEventCycleStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCameraEventCycleStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newStrictCameraEventCycle("code-520", "title-720")
        item.status = StrictCameraEventCycleStatus.CLOSED
        assertFails("move") { item.moveTo(StrictCameraEventCycleStatus.DRAFT) }
    }
}
