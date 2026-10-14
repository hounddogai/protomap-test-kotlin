package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedViolationRollupTest {
    @Test
    fun `starts in new`() {
        val item = newWeightedViolationRollup("code-158", "name-95")
        assertEquals(WeightedViolationRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newWeightedViolationRollup("code-158", "name-95")
        try {
            item.moveTo(WeightedViolationRollupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedViolationRollupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newWeightedViolationRollup("code-158", "name-95")
        item.status = WeightedViolationRollupStatus.FAILED
        assertFails("move") { item.moveTo(WeightedViolationRollupStatus.NEW) }
    }
}
