package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newLanePlanPreference("code-738", "label-431")
        assertEquals(LanePlanPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLanePlanPreference("code-738", "label-431")
        try {
            item.moveTo(LanePlanPreferenceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanPreferenceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLanePlanPreference("code-738", "label-431")
        item.status = LanePlanPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(LanePlanPreferenceStatus.IDLE) }
    }
}
