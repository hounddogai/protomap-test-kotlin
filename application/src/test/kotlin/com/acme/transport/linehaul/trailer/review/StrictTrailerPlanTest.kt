package com.acme.transport.linehaul.trailer.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictTrailerPlan("code-8", "label-865")
        assertEquals(StrictTrailerPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newStrictTrailerPlan("code-8", "label-865")
        try {
            item.moveTo(StrictTrailerPlanStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerPlanStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictTrailerPlan("code-8", "label-865")
        item.status = StrictTrailerPlanStatus.STOPPED
        assertFails("move") { item.moveTo(StrictTrailerPlanStatus.IDLE) }
    }
}
