package com.acme.transport.emissions.carbonreport.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CarbonReportReservationTest {
    @Test
    fun `starts in planned`() {
        val item = newCarbonReportReservation("code-564", "name-159")
        assertEquals(CarbonReportReservationStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCarbonReportReservation("code-564", "name-159")
        try {
            item.moveTo(CarbonReportReservationStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CarbonReportReservationStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCarbonReportReservation("code-564", "name-159")
        item.status = CarbonReportReservationStatus.CANCELLED
        assertFails("move") { item.moveTo(CarbonReportReservationStatus.PLANNED) }
    }
}
