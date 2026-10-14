package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAuditThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newRollingAuditThreshold("code-636", "name-685")
        assertEquals(RollingAuditThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRollingAuditThreshold("code-636", "name-685")
        try {
            item.moveTo(RollingAuditThresholdStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAuditThresholdStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingAuditThreshold("code-636", "name-685")
        item.status = RollingAuditThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingAuditThresholdStatus.OPEN) }
    }
}
