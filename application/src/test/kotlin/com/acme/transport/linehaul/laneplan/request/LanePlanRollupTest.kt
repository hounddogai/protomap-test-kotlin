package com.acme.transport.linehaul.laneplan.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newLanePlanRollup("code-103", "title-861")
        assertEquals(LanePlanRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLanePlanRollup("code-103", "title-861")
        try {
            item.moveTo(LanePlanRollupStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanRollupStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLanePlanRollup("code-103", "title-861")
        item.status = LanePlanRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(LanePlanRollupStatus.REQUESTED) }
    }
}
