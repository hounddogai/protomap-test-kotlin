package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDropTargetTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedDropTarget("code-878", "label-808")
        assertEquals(WeightedDropTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedDropTarget("code-878", "label-808")
        try {
            item.moveTo(WeightedDropTargetStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDropTargetStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedDropTarget("code-878", "label-808")
        item.status = WeightedDropTargetStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedDropTargetStatus.CREATED) }
    }
}
