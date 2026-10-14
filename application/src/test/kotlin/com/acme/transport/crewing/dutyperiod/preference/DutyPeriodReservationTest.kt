package com.acme.transport.crewing.dutyperiod.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodReservationTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodReservation("code-681", "title-538")
        assertEquals(DutyPeriodReservationStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDutyPeriodReservation("code-681", "title-538")
        try {
            item.moveTo(DutyPeriodReservationStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodReservationStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodReservation("code-681", "title-538")
        item.status = DutyPeriodReservationStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodReservationStatus.PROPOSED) }
    }
}
