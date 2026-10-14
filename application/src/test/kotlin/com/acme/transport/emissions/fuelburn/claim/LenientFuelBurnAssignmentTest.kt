package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelBurnAssignmentTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientFuelBurnAssignment("code-441", "title-505")
        assertEquals(LenientFuelBurnAssignmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientFuelBurnAssignment("code-441", "title-505")
        try {
            item.moveTo(LenientFuelBurnAssignmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelBurnAssignmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientFuelBurnAssignment("code-441", "title-505")
        item.status = LenientFuelBurnAssignmentStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientFuelBurnAssignmentStatus.REQUESTED) }
    }
}
