package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaScheduleTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaSchedule("code-692", "title-802")
        assertEquals(ServiceAreaScheduleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newServiceAreaSchedule("code-692", "title-802")
        try {
            item.moveTo(ServiceAreaScheduleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaScheduleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaSchedule("code-692", "title-802")
        item.status = ServiceAreaScheduleStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaScheduleStatus.DRAFT) }
    }
}
