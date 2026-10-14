package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedViolationCycleTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedViolationCycle("code-972", "name-307")
        assertEquals(WeightedViolationCycleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedViolationCycle("code-972", "name-307")
        try {
            item.moveTo(WeightedViolationCycleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedViolationCycleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedViolationCycle("code-972", "name-307")
        item.status = WeightedViolationCycleStatus.FAILED
        assertFails("move") { item.moveTo(WeightedViolationCycleStatus.NEW) }
    }
}
