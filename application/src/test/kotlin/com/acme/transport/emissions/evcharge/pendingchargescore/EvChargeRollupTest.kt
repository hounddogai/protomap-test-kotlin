package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeRollupTest {
    @Test
    fun `starts in open`() {
        val item = newEvChargeRollup("code-580", "name-765")
        assertEquals(EvChargeRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newEvChargeRollup("code-580", "name-765")
        try {
            item.moveTo(EvChargeRollupStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeRollupStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newEvChargeRollup("code-580", "name-765")
        item.status = EvChargeRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(EvChargeRollupStatus.OPEN) }
    }
}
