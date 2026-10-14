package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldRollupTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledHoldRollup("code-811", "title-717")
        assertEquals(ScheduledHoldRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newScheduledHoldRollup("code-811", "title-717")
        try {
            item.moveTo(ScheduledHoldRollupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldRollupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledHoldRollup("code-811", "title-717")
        item.status = ScheduledHoldRollupStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledHoldRollupStatus.CREATED) }
    }
}
