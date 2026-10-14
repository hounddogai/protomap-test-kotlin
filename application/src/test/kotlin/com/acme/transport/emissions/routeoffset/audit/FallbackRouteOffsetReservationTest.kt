package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackRouteOffsetReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackRouteOffsetReservation("code-83", "title-666")
        assertEquals(FallbackRouteOffsetReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFallbackRouteOffsetReservation("code-83", "title-666")
        try {
            item.moveTo(FallbackRouteOffsetReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackRouteOffsetReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackRouteOffsetReservation("code-83", "title-666")
        item.status = FallbackRouteOffsetReservationStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackRouteOffsetReservationStatus.DRAFT) }
    }
}
