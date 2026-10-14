package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ViolationReservationTest {
    @Test
    fun `starts in created`() {
        val item = newViolationReservation("code-432", "label-966")
        assertEquals(ViolationReservationStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newViolationReservation("code-432", "label-966")
        try {
            item.moveTo(ViolationReservationStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ViolationReservationStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newViolationReservation("code-432", "label-966")
        item.status = ViolationReservationStatus.RETIRED
        assertFails("move") { item.moveTo(ViolationReservationStatus.CREATED) }
    }
}
