package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveServicePlanManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveServicePlanManifest("code-627", "title-742")
        assertEquals(AdaptiveServicePlanManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveServicePlanManifest("code-627", "title-742")
        try {
            item.moveTo(AdaptiveServicePlanManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveServicePlanManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveServicePlanManifest("code-627", "title-742")
        item.status = AdaptiveServicePlanManifestStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveServicePlanManifestStatus.IDLE) }
    }
}
