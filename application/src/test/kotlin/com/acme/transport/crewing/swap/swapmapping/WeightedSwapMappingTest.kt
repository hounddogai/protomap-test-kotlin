package com.acme.transport.crewing.swap.swapmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedSwapMappingTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedSwapMapping("code-542", "label-333")
        assertEquals(WeightedSwapMappingStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedSwapMapping("code-542", "label-333")
        try {
            item.moveTo(WeightedSwapMappingStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedSwapMappingStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedSwapMapping("code-542", "label-333")
        item.status = WeightedSwapMappingStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedSwapMappingStatus.REQUESTED) }
    }
}
