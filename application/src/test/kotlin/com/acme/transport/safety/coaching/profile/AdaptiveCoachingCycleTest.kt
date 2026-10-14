package com.acme.transport.safety.coaching.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveCoachingCycleTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveCoachingCycle("code-645", "name-809")
        assertEquals(AdaptiveCoachingCycleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveCoachingCycle("code-645", "name-809")
        try {
            item.moveTo(AdaptiveCoachingCycleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveCoachingCycleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveCoachingCycle("code-645", "name-809")
        item.status = AdaptiveCoachingCycleStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveCoachingCycleStatus.IDLE) }
    }
}
