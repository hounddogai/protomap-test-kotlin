package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLanePlanManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedLanePlanManifest("code-558", "title-293")
        assertEquals(WeightedLanePlanManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedLanePlanManifest("code-558", "title-293")
        try {
            item.moveTo(WeightedLanePlanManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLanePlanManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedLanePlanManifest("code-558", "title-293")
        item.status = WeightedLanePlanManifestStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedLanePlanManifestStatus.DRAFT) }
    }
}
