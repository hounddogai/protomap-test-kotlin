package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedLanePlanThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        assertEquals(CappedLanePlanThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        try {
            item.moveTo(CappedLanePlanThresholdStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedLanePlanThresholdStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedLanePlanThreshold("code-507", "name-18")
        item.status = CappedLanePlanThresholdStatus.FAILED
        assertFails("move") { item.moveTo(CappedLanePlanThresholdStatus.NEW) }
    }
}
