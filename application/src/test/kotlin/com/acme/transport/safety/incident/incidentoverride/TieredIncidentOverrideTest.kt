package com.acme.transport.safety.incident.incidentoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIncidentOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newTieredIncidentOverride("code-774", "name-993")
        assertEquals(TieredIncidentOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTieredIncidentOverride("code-774", "name-993")
        try {
            item.moveTo(TieredIncidentOverrideStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIncidentOverrideStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredIncidentOverride("code-774", "name-993")
        item.status = TieredIncidentOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredIncidentOverrideStatus.OPEN) }
    }
}
