package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedIncidentSnapshotTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedIncidentSnapshot("code-505", "label-75")
        assertEquals(WeightedIncidentSnapshotStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newWeightedIncidentSnapshot("code-505", "label-75")
        try {
            item.moveTo(WeightedIncidentSnapshotStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedIncidentSnapshotStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedIncidentSnapshot("code-505", "label-75")
        item.status = WeightedIncidentSnapshotStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedIncidentSnapshotStatus.CREATED) }
    }
}
