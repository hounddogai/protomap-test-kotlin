package com.acme.transport.linehaul.backhaul.backhaulreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBackhaulWindowTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        assertEquals(LegacyBackhaulWindowStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        try {
            item.moveTo(LegacyBackhaulWindowStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBackhaulWindowStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyBackhaulWindow("code-956", "title-361")
        item.status = LegacyBackhaulWindowStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyBackhaulWindowStatus.OPEN) }
    }
}
