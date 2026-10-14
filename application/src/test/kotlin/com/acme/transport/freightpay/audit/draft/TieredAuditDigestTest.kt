package com.acme.transport.freightpay.audit.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAuditDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredAuditDigest("code-460", "name-781")
        assertEquals(TieredAuditDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newTieredAuditDigest("code-460", "name-781")
        try {
            item.moveTo(TieredAuditDigestStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAuditDigestStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredAuditDigest("code-460", "name-781")
        item.status = TieredAuditDigestStatus.STOPPED
        assertFails("move") { item.moveTo(TieredAuditDigestStatus.IDLE) }
    }
}
