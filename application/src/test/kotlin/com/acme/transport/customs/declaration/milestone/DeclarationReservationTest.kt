package com.acme.transport.customs.declaration.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DeclarationReservationTest {
    @Test
    fun `starts in draft`() {
        val item = newDeclarationReservation("code-422", "label-321")
        assertEquals(DeclarationReservationStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDeclarationReservation("code-422", "label-321")
        try {
            item.moveTo(DeclarationReservationStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DeclarationReservationStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDeclarationReservation("code-422", "label-321")
        item.status = DeclarationReservationStatus.CLOSED
        assertFails("move") { item.moveTo(DeclarationReservationStatus.DRAFT) }
    }
}
