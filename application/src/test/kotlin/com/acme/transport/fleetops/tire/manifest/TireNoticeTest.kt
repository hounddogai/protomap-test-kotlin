package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newTireNotice("code-304", "name-748")
        assertEquals(TireNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTireNotice("code-304", "name-748")
        try {
            item.moveTo(TireNoticeStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireNoticeStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTireNotice("code-304", "name-748")
        item.status = TireNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(TireNoticeStatus.REQUESTED) }
    }
}
