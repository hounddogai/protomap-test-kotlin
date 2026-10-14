package com.acme.transport.freightpay.accessorial.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newAccessorialQuota("code-66", "title-581")
        assertEquals(AccessorialQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newAccessorialQuota("code-66", "title-581")
        try {
            item.moveTo(AccessorialQuotaStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialQuotaStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAccessorialQuota("code-66", "title-581")
        item.status = AccessorialQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(AccessorialQuotaStatus.OPEN) }
    }
}
