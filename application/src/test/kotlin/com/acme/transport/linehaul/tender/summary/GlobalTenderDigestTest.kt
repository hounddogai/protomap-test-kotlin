package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTenderDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalTenderDigest("code-342", "title-212")
        assertEquals(GlobalTenderDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalTenderDigest("code-342", "title-212")
        try {
            item.moveTo(GlobalTenderDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTenderDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalTenderDigest("code-342", "title-212")
        item.status = GlobalTenderDigestStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalTenderDigestStatus.IDLE) }
    }
}
