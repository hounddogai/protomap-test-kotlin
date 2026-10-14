package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedInspectionRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedInspectionRollup("code-288", "title-211")
        assertEquals(WeightedInspectionRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newWeightedInspectionRollup("code-288", "title-211")
        try {
            item.moveTo(WeightedInspectionRollupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedInspectionRollupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedInspectionRollup("code-288", "title-211")
        item.status = WeightedInspectionRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedInspectionRollupStatus.PROPOSED) }
    }
}
