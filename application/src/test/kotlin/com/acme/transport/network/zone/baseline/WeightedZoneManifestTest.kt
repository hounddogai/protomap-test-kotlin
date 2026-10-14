package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedZoneManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedZoneManifest("code-452", "label-650")
        assertEquals(WeightedZoneManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newWeightedZoneManifest("code-452", "label-650")
        try {
            item.moveTo(WeightedZoneManifestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedZoneManifestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedZoneManifest("code-452", "label-650")
        item.status = WeightedZoneManifestStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedZoneManifestStatus.IDLE) }
    }
}
