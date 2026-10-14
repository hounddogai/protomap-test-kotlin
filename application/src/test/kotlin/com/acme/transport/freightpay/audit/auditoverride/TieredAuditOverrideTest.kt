package com.acme.transport.freightpay.audit.auditoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAuditOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredAuditOverride("code-372", "title-955")
        assertEquals(TieredAuditOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTieredAuditOverride("code-372", "title-955")
        try {
            item.moveTo(TieredAuditOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAuditOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredAuditOverride("code-372", "title-955")
        item.status = TieredAuditOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(TieredAuditOverrideStatus.IDLE) }
    }
}
