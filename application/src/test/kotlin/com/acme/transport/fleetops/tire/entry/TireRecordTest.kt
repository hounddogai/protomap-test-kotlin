package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newTireRecord("code-679", "label-534")
        assertEquals(TireRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newTireRecord("code-679", "label-534")
        try {
            item.moveTo(TireRecordStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireRecordStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTireRecord("code-679", "label-534")
        item.status = TireRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(TireRecordStatus.PLANNED) }
    }
}
