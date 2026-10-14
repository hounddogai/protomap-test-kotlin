package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledLanePlanOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        assertEquals(ScheduledLanePlanOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        try {
            item.moveTo(ScheduledLanePlanOverrideStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledLanePlanOverrideStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledLanePlanOverride("code-90", "name-747")
        item.status = ScheduledLanePlanOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledLanePlanOverrideStatus.PROPOSED) }
    }
}
