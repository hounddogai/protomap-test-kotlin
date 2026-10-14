package com.acme.transport.linehaul.laneplan.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanBundle("code-480", "name-168")
        assertEquals(LanePlanBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newLanePlanBundle("code-480", "name-168")
        try {
            item.moveTo(LanePlanBundleStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanBundleStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanBundle("code-480", "name-168")
        item.status = LanePlanBundleStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanBundleStatus.IDLE) }
    }
}
