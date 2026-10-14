package com.acme.transport.linehaul.laneplan.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredLanePlanOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newTieredLanePlanOverride("code-975", "title-565")
        assertEquals(TieredLanePlanOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredLanePlanOverride("code-975", "title-565")
        try {
            item.moveTo(TieredLanePlanOverrideStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredLanePlanOverrideStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredLanePlanOverride("code-975", "title-565")
        item.status = TieredLanePlanOverrideStatus.FAILED
        assertFails("move") { item.moveTo(TieredLanePlanOverrideStatus.NEW) }
    }
}
