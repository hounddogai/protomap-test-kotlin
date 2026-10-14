package com.acme.transport.freightpay.accessorial.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialNotice("code-421", "label-329")
        assertEquals(AccessorialNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAccessorialNotice("code-421", "label-329")
        try {
            item.moveTo(AccessorialNoticeStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialNoticeStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialNotice("code-421", "label-329")
        item.status = AccessorialNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialNoticeStatus.REQUESTED) }
    }
}
