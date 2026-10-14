package com.acme.transport.safety.incident.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredIncidentBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newTieredIncidentBaseline("code-763", "name-806")
        assertEquals(TieredIncidentBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredIncidentBaseline("code-763", "name-806")
        try {
            item.moveTo(TieredIncidentBaselineStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredIncidentBaselineStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredIncidentBaseline("code-763", "name-806")
        item.status = TieredIncidentBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(TieredIncidentBaselineStatus.CREATED) }
    }
}
