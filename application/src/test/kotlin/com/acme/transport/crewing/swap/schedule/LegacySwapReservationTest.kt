package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySwapReservationTest {
    @Test
    fun `starts in created`() {
        val item = newLegacySwapReservation("code-84", "label-397")
        assertEquals(LegacySwapReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLegacySwapReservation("code-84", "label-397")
        try {
            item.moveTo(LegacySwapReservationStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySwapReservationStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacySwapReservation("code-84", "label-397")
        item.status = LegacySwapReservationStatus.RETIRED
        assertFails("move") { item.moveTo(LegacySwapReservationStatus.CREATED) }
    }
}
