package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledZoneRollupTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledZoneRollup("code-616", "name-177")
        assertEquals(ScheduledZoneRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledZoneRollup("code-616", "name-177")
        try {
            item.moveTo(ScheduledZoneRollupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledZoneRollupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledZoneRollup("code-616", "name-177")
        item.status = ScheduledZoneRollupStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledZoneRollupStatus.CREATED) }
    }
}
