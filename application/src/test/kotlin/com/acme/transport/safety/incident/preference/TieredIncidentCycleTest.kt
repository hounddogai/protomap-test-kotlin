package com.acme.transport.safety.incident.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIncidentCycleTest {
    @Test
    fun `starts in open`() {
        val item = newTieredIncidentCycle("code-766", "name-832")
        assertEquals(TieredIncidentCycleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTieredIncidentCycle("code-766", "name-832")
        try {
            item.moveTo(TieredIncidentCycleStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIncidentCycleStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredIncidentCycle("code-766", "name-832")
        item.status = TieredIncidentCycleStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredIncidentCycleStatus.OPEN) }
    }
}
