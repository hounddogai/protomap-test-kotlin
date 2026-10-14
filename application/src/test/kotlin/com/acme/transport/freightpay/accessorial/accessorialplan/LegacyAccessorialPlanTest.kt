package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAccessorialPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyAccessorialPlan("code-580", "name-227")
        assertEquals(LegacyAccessorialPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLegacyAccessorialPlan("code-580", "name-227")
        try {
            item.moveTo(LegacyAccessorialPlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAccessorialPlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyAccessorialPlan("code-580", "name-227")
        item.status = LegacyAccessorialPlanStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyAccessorialPlanStatus.IDLE) }
    }
}
