package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialGroupTest {
    @Test
    fun `starts in new`() {
        val item = newAccessorialGroup("code-252", "label-39")
        assertEquals(AccessorialGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAccessorialGroup("code-252", "label-39")
        try {
            item.moveTo(AccessorialGroupStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialGroupStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAccessorialGroup("code-252", "label-39")
        item.status = AccessorialGroupStatus.FAILED
        assertFails("move") { item.moveTo(AccessorialGroupStatus.NEW) }
    }
}
