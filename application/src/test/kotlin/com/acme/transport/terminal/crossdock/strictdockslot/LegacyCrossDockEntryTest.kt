package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyCrossDockEntryTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyCrossDockEntry("code-3", "label-566")
        assertEquals(LegacyCrossDockEntryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newLegacyCrossDockEntry("code-3", "label-566")
        try {
            item.moveTo(LegacyCrossDockEntryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyCrossDockEntryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyCrossDockEntry("code-3", "label-566")
        item.status = LegacyCrossDockEntryStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyCrossDockEntryStatus.OPEN) }
    }
}
