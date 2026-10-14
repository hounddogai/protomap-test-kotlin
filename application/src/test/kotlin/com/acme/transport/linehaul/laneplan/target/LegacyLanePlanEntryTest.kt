package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyLanePlanEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyLanePlanEntry("code-867", "label-977")
        assertEquals(LegacyLanePlanEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyLanePlanEntry("code-867", "label-977")
        try {
            item.moveTo(LegacyLanePlanEntryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyLanePlanEntryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyLanePlanEntry("code-867", "label-977")
        item.status = LegacyLanePlanEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyLanePlanEntryStatus.REQUESTED) }
    }
}
