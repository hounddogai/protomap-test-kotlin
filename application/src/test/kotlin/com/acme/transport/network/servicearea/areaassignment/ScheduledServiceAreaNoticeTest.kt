package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledServiceAreaNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newScheduledServiceAreaNotice("code-887", "name-352")
        assertEquals(ScheduledServiceAreaNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newScheduledServiceAreaNotice("code-887", "name-352")
        try {
            item.moveTo(ScheduledServiceAreaNoticeStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledServiceAreaNoticeStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newScheduledServiceAreaNotice("code-887", "name-352")
        item.status = ScheduledServiceAreaNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ScheduledServiceAreaNoticeStatus.IDLE) }
    }
}
