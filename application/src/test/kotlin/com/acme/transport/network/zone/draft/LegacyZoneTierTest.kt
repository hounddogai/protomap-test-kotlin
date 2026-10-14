package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyZoneTierTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyZoneTier("code-810", "name-716")
        assertEquals(LegacyZoneTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLegacyZoneTier("code-810", "name-716")
        try {
            item.moveTo(LegacyZoneTierStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyZoneTierStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyZoneTier("code-810", "name-716")
        item.status = LegacyZoneTierStatus.FAILED
        assertFails("move") { item.moveTo(LegacyZoneTierStatus.NEW) }
    }
}
