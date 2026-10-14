package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLanePlanBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedLanePlanBundle("code-295", "label-449")
        assertEquals(WeightedLanePlanBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedLanePlanBundle("code-295", "label-449")
        try {
            item.moveTo(WeightedLanePlanBundleStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLanePlanBundleStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedLanePlanBundle("code-295", "label-449")
        item.status = WeightedLanePlanBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedLanePlanBundleStatus.PLANNED) }
    }
}
