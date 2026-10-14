package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredLanePlanRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredLanePlanRequest("code-835", "label-22")
        assertEquals(TieredLanePlanRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredLanePlanRequest("code-835", "label-22")
        try {
            item.moveTo(TieredLanePlanRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredLanePlanRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredLanePlanRequest("code-835", "label-22")
        item.status = TieredLanePlanRequestStatus.STOPPED
        assertFails("move") { item.moveTo(TieredLanePlanRequestStatus.IDLE) }
    }
}
