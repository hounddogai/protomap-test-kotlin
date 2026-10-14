package com.acme.transport.linehaul.laneplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredLanePlanThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredLanePlanThreshold("code-56", "name-465")
        assertEquals(TieredLanePlanThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredLanePlanThreshold("code-56", "name-465")
        try {
            item.moveTo(TieredLanePlanThresholdStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredLanePlanThresholdStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredLanePlanThreshold("code-56", "name-465")
        item.status = TieredLanePlanThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(TieredLanePlanThresholdStatus.IDLE) }
    }
}
