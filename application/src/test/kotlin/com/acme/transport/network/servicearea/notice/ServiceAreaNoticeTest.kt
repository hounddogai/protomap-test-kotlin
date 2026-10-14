package com.acme.transport.network.servicearea.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaNoticeTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaNotice("code-699", "name-340")
        assertEquals(ServiceAreaNoticeStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newServiceAreaNotice("code-699", "name-340")
        try {
            item.moveTo(ServiceAreaNoticeStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaNoticeStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaNotice("code-699", "name-340")
        item.status = ServiceAreaNoticeStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaNoticeStatus.IDLE) }
    }
}
