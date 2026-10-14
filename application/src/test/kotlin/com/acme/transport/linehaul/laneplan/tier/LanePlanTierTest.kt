package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanTierTest {
    @Test
    fun `starts in new`() {
        val item = newLanePlanTier("code-695", "name-714")
        assertEquals(LanePlanTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLanePlanTier("code-695", "name-714")
        try {
            item.moveTo(LanePlanTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLanePlanTier("code-695", "name-714")
        item.status = LanePlanTierStatus.FAILED
        assertFails("move") { item.moveTo(LanePlanTierStatus.NEW) }
    }
}
