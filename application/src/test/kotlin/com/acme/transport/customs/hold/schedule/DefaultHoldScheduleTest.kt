package com.acme.transport.customs.hold.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHoldScheduleTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultHoldSchedule("code-524", "title-272")
        assertEquals(DefaultHoldScheduleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newDefaultHoldSchedule("code-524", "title-272")
        try {
            item.moveTo(DefaultHoldScheduleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHoldScheduleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultHoldSchedule("code-524", "title-272")
        item.status = DefaultHoldScheduleStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultHoldScheduleStatus.CREATED) }
    }
}
