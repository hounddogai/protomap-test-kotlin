package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDeclarationReservationTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingDeclarationReservation("code-799", "label-205")
        assertEquals(PendingDeclarationReservationStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingDeclarationReservation("code-799", "label-205")
        try {
            item.moveTo(PendingDeclarationReservationStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDeclarationReservationStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingDeclarationReservation("code-799", "label-205")
        item.status = PendingDeclarationReservationStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingDeclarationReservationStatus.REQUESTED) }
    }
}
