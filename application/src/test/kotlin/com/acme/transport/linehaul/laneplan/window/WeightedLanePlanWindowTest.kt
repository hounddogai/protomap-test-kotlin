package com.acme.transport.linehaul.laneplan.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLanePlanWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedLanePlanWindow("code-418", "name-65")
        assertEquals(WeightedLanePlanWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedLanePlanWindow("code-418", "name-65")
        try {
            item.moveTo(WeightedLanePlanWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLanePlanWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedLanePlanWindow("code-418", "name-65")
        item.status = WeightedLanePlanWindowStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedLanePlanWindowStatus.DRAFT) }
    }
}
