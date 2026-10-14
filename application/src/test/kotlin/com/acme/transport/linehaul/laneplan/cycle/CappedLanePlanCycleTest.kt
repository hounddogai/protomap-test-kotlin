package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanCycleTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedLanePlanCycle("code-853", "title-313")
        assertEquals(CappedLanePlanCycleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedLanePlanCycle("code-853", "title-313")
        try {
            item.moveTo(CappedLanePlanCycleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanCycleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedLanePlanCycle("code-853", "title-313")
        item.status = CappedLanePlanCycleStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedLanePlanCycleStatus.REQUESTED) }
    }
}
