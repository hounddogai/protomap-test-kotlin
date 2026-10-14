package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newHubSchedule("code-100", "title-895")
        assertEquals(HubScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newHubSchedule("code-100", "title-895")
        try {
            item.moveTo(HubScheduleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubScheduleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newHubSchedule("code-100", "title-895")
        item.status = HubScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(HubScheduleStatus.PROPOSED) }
    }
}
