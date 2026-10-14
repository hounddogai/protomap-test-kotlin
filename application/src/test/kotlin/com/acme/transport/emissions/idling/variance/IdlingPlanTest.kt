package com.acme.transport.emissions.idling.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newIdlingPlan("code-138", "title-193")
        assertEquals(IdlingPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newIdlingPlan("code-138", "title-193")
        try {
            item.moveTo(IdlingPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newIdlingPlan("code-138", "title-193")
        item.status = IdlingPlanStatus.STOPPED
        assertFails("move") { item.moveTo(IdlingPlanStatus.IDLE) }
    }
}
