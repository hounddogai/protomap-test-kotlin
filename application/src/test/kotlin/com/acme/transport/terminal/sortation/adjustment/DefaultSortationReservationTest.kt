package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultSortationReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultSortationReservation("code-451", "title-854")
        assertEquals(DefaultSortationReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newDefaultSortationReservation("code-451", "title-854")
        try {
            item.moveTo(DefaultSortationReservationStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultSortationReservationStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultSortationReservation("code-451", "title-854")
        item.status = DefaultSortationReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultSortationReservationStatus.REQUESTED) }
    }
}
