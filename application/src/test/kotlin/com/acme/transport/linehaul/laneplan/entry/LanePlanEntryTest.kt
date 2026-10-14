package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanEntryTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanEntry("code-874", "name-632")
        assertEquals(LanePlanEntryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLanePlanEntry("code-874", "name-632")
        try {
            item.moveTo(LanePlanEntryStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanEntryStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanEntry("code-874", "name-632")
        item.status = LanePlanEntryStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanEntryStatus.IDLE) }
    }
}
