package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBackhaulThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        assertEquals(WeightedBackhaulThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        try {
            item.moveTo(WeightedBackhaulThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBackhaulThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        item.status = WeightedBackhaulThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedBackhaulThresholdStatus.CREATED) }
    }
}
