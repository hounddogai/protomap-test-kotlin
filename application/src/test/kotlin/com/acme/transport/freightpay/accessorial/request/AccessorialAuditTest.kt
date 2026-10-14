package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialAuditTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialAudit("code-864", "label-944")
        assertEquals(AccessorialAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newAccessorialAudit("code-864", "label-944")
        try {
            item.moveTo(AccessorialAuditStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialAuditStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialAudit("code-864", "label-944")
        item.status = AccessorialAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialAuditStatus.OPEN) }
    }
}
