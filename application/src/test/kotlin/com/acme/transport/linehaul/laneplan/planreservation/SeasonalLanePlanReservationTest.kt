package com.acme.transport.linehaul.laneplan.planreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalLanePlanReservationTest {
    @Test
    fun `starts in open`() {
        val item = newSeasonalLanePlanReservation("code-819", "title-592")
        assertEquals(SeasonalLanePlanReservationStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newSeasonalLanePlanReservation("code-819", "title-592")
        try {
            item.moveTo(SeasonalLanePlanReservationStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalLanePlanReservationStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newSeasonalLanePlanReservation("code-819", "title-592")
        item.status = SeasonalLanePlanReservationStatus.ARCHIVED
        assertFails("move") { item.moveTo(SeasonalLanePlanReservationStatus.OPEN) }
    }
}
