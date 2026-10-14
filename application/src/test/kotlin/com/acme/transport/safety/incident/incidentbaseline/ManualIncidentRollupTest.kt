package com.acme.transport.safety.incident.incidentbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIncidentRollupTest {
    @Test
    fun `starts in new`() {
        val item = newManualIncidentRollup("code-9", "title-424")
        assertEquals(ManualIncidentRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newManualIncidentRollup("code-9", "title-424")
        try {
            item.moveTo(ManualIncidentRollupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIncidentRollupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualIncidentRollup("code-9", "title-424")
        item.status = ManualIncidentRollupStatus.FAILED
        assertFails("move") { item.moveTo(ManualIncidentRollupStatus.NEW) }
    }
}
