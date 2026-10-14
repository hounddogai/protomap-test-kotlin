package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServicePlanManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        assertEquals(WeightedServicePlanManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        try {
            item.moveTo(WeightedServicePlanManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServicePlanManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        item.status = WeightedServicePlanManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedServicePlanManifestStatus.REQUESTED) }
    }
}
