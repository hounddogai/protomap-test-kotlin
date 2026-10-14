package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTireRecordTest {
    @Test
    fun `starts in new`() {
        val item = newStrictTireRecord("code-476", "label-53")
        assertEquals(StrictTireRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newStrictTireRecord("code-476", "label-53")
        try {
            item.moveTo(StrictTireRecordStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTireRecordStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictTireRecord("code-476", "label-53")
        item.status = StrictTireRecordStatus.FAILED
        assertFails("move") { item.moveTo(StrictTireRecordStatus.NEW) }
    }
}
