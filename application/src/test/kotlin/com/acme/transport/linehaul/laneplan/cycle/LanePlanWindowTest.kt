package com.acme.transport.linehaul.laneplan.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanWindowTest {
    @Test
    fun `starts in requested`() {
        val item = newLanePlanWindow("code-207", "name-966")
        assertEquals(LanePlanWindowStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLanePlanWindow("code-207", "name-966")
        try {
            item.moveTo(LanePlanWindowStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanWindowStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLanePlanWindow("code-207", "name-966")
        item.status = LanePlanWindowStatus.EXPIRED
        assertFails("move") { item.moveTo(LanePlanWindowStatus.REQUESTED) }
    }
}
