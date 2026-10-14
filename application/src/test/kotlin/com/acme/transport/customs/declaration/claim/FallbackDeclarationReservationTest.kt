package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDeclarationReservationTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        assertEquals(FallbackDeclarationReservationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        try {
            item.moveTo(FallbackDeclarationReservationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDeclarationReservationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackDeclarationReservation("code-807", "name-202")
        item.status = FallbackDeclarationReservationStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackDeclarationReservationStatus.IDLE) }
    }
}
