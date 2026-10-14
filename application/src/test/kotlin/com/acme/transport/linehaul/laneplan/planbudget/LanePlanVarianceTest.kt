package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanVariance("code-69", "title-869")
        assertEquals(LanePlanVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLanePlanVariance("code-69", "title-869")
        try {
            item.moveTo(LanePlanVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanVariance("code-69", "title-869")
        item.status = LanePlanVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanVarianceStatus.IDLE) }
    }
}
