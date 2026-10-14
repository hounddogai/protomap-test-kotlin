package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFuelBurnProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientFuelBurnProfile("code-602", "title-67")
        assertEquals(LenientFuelBurnProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientFuelBurnProfile("code-602", "title-67")
        try {
            item.moveTo(LenientFuelBurnProfileStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFuelBurnProfileStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientFuelBurnProfile("code-602", "title-67")
        item.status = LenientFuelBurnProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientFuelBurnProfileStatus.PROPOSED) }
    }
}
