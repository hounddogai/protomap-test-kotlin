package com.acme.transport.network.cutoff.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCutoffReservationTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyCutoffReservation("code-47", "label-248")
        assertEquals(LegacyCutoffReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyCutoffReservation("code-47", "label-248")
        try {
            item.moveTo(LegacyCutoffReservationStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCutoffReservationStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyCutoffReservation("code-47", "label-248")
        item.status = LegacyCutoffReservationStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyCutoffReservationStatus.CREATED) }
    }
}
