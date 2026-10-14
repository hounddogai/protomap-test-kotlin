package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newAccessorialRecord("code-86", "label-613")
        assertEquals(AccessorialRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newAccessorialRecord("code-86", "label-613")
        try {
            item.moveTo(AccessorialRecordStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialRecordStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newAccessorialRecord("code-86", "label-613")
        item.status = AccessorialRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(AccessorialRecordStatus.REQUESTED) }
    }
}
