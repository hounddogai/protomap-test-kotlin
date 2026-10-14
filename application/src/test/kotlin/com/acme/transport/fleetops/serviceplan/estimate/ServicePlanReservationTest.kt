package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServicePlanReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newServicePlanReservation("code-794", "name-655")
        assertEquals(ServicePlanReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newServicePlanReservation("code-794", "name-655")
        try {
            item.moveTo(ServicePlanReservationStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServicePlanReservationStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newServicePlanReservation("code-794", "name-655")
        item.status = ServicePlanReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(ServicePlanReservationStatus.REQUESTED) }
    }
}
