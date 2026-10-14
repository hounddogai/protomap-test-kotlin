package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyFuelBurnDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyFuelBurnDraft("code-398", "name-769")
        assertEquals(LegacyFuelBurnDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyFuelBurnDraft("code-398", "name-769")
        try {
            item.moveTo(LegacyFuelBurnDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyFuelBurnDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyFuelBurnDraft("code-398", "name-769")
        item.status = LegacyFuelBurnDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyFuelBurnDraftStatus.REQUESTED) }
    }
}
