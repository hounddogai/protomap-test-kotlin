package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSortationBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newWeightedSortationBundle("code-22", "name-947")
        assertEquals(WeightedSortationBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newWeightedSortationBundle("code-22", "name-947")
        try {
            item.moveTo(WeightedSortationBundleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSortationBundleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newWeightedSortationBundle("code-22", "name-947")
        item.status = WeightedSortationBundleStatus.STOPPED
        assertFails("move") { item.moveTo(WeightedSortationBundleStatus.IDLE) }
    }
}
