package com.acme.transport.crewing.restbreak.regionalbreakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedRestBreakPreference("code-111", "name-268")
        assertEquals(WeightedRestBreakPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newWeightedRestBreakPreference("code-111", "name-268")
        try {
            item.moveTo(WeightedRestBreakPreferenceStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakPreferenceStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedRestBreakPreference("code-111", "name-268")
        item.status = WeightedRestBreakPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedRestBreakPreferenceStatus.PLANNED) }
    }
}
