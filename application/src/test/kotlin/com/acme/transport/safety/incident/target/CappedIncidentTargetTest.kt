package com.acme.transport.safety.incident.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedIncidentTargetTest {
    @Test
    fun `starts in created`() {
        val item = newCappedIncidentTarget("code-349", "title-485")
        assertEquals(CappedIncidentTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedIncidentTarget("code-349", "title-485")
        try {
            item.moveTo(CappedIncidentTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedIncidentTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedIncidentTarget("code-349", "title-485")
        item.status = CappedIncidentTargetStatus.RETIRED
        assertFails("move") { item.moveTo(CappedIncidentTargetStatus.CREATED) }
    }
}
