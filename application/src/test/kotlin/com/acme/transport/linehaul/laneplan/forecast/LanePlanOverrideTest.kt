package com.acme.transport.linehaul.laneplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newLanePlanOverride("code-230", "label-21")
        assertEquals(LanePlanOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLanePlanOverride("code-230", "label-21")
        try {
            item.moveTo(LanePlanOverrideStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanOverrideStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLanePlanOverride("code-230", "label-21")
        item.status = LanePlanOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(LanePlanOverrideStatus.DRAFT) }
    }
}
