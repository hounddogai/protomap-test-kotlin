package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDefectSlotTest {
    @Test
    fun `starts in new`() {
        val item = newStrictDefectSlot("code-314", "name-575")
        assertEquals(StrictDefectSlotStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newStrictDefectSlot("code-314", "name-575")
        try {
            item.moveTo(StrictDefectSlotStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDefectSlotStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictDefectSlot("code-314", "name-575")
        item.status = StrictDefectSlotStatus.FAILED
        assertFails("move") { item.moveTo(StrictDefectSlotStatus.NEW) }
    }
}
