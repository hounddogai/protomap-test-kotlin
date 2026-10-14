package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLanePlanTargetTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedLanePlanTarget("code-113", "name-526")
        assertEquals(WeightedLanePlanTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newWeightedLanePlanTarget("code-113", "name-526")
        try {
            item.moveTo(WeightedLanePlanTargetStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLanePlanTargetStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedLanePlanTarget("code-113", "name-526")
        item.status = WeightedLanePlanTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedLanePlanTargetStatus.OPEN) }
    }
}
