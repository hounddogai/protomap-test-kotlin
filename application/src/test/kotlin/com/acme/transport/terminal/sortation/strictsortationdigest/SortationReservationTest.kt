package com.acme.transport.terminal.sortation.strictsortationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationReservationTest {
    @Test
    fun `starts in new`() {
        val item = newSortationReservation("code-718", "title-198")
        assertEquals(SortationReservationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSortationReservation("code-718", "title-198")
        try {
            item.moveTo(SortationReservationStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationReservationStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSortationReservation("code-718", "title-198")
        item.status = SortationReservationStatus.FAILED
        assertFails("move") { item.moveTo(SortationReservationStatus.NEW) }
    }
}
