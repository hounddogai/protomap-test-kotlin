package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedRelayScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newCappedRelaySchedule("code-836", "title-301")
        assertEquals(CappedRelayScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedRelaySchedule("code-836", "title-301")
        try {
            item.moveTo(CappedRelayScheduleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedRelayScheduleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedRelaySchedule("code-836", "title-301")
        item.status = CappedRelayScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(CappedRelayScheduleStatus.CREATED) }
    }
}
