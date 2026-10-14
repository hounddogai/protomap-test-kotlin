package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedCapacityPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedCapacityPreference("code-270", "name-181")
        assertEquals(WeightedCapacityPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newWeightedCapacityPreference("code-270", "name-181")
        try {
            item.moveTo(WeightedCapacityPreferenceStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedCapacityPreferenceStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedCapacityPreference("code-270", "name-181")
        item.status = WeightedCapacityPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedCapacityPreferenceStatus.OPEN) }
    }
}
