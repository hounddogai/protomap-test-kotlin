package com.acme.transport.network.zone.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneRollupTest {
    @Test
    fun `starts in created`() {
        val item = newZoneRollup("code-532", "label-643")
        assertEquals(ZoneRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newZoneRollup("code-532", "label-643")
        try {
            item.moveTo(ZoneRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newZoneRollup("code-532", "label-643")
        item.status = ZoneRollupStatus.RETIRED
        assertFails("move") { item.moveTo(ZoneRollupStatus.CREATED) }
    }
}
