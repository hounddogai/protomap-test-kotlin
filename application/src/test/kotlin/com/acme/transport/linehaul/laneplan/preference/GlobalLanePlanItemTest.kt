package com.acme.transport.linehaul.laneplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalLanePlanItemTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalLanePlanItem("code-615", "title-478")
        assertEquals(GlobalLanePlanItemStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newGlobalLanePlanItem("code-615", "title-478")
        try {
            item.moveTo(GlobalLanePlanItemStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalLanePlanItemStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalLanePlanItem("code-615", "title-478")
        item.status = GlobalLanePlanItemStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalLanePlanItemStatus.IDLE) }
    }
}
