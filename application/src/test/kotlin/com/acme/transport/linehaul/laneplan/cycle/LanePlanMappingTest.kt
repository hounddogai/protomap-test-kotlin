package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanMappingTest {
    @Test
    fun `starts in new`() {
        val item = newLanePlanMapping("code-578", "label-820")
        assertEquals(LanePlanMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newLanePlanMapping("code-578", "label-820")
        try {
            item.moveTo(LanePlanMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLanePlanMapping("code-578", "label-820")
        item.status = LanePlanMappingStatus.FAILED
        assertFails("move") { item.moveTo(LanePlanMappingStatus.NEW) }
    }
}
