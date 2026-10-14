package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newEvChargeAudit("code-68", "label-46")
        assertEquals(EvChargeAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newEvChargeAudit("code-68", "label-46")
        try {
            item.moveTo(EvChargeAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newEvChargeAudit("code-68", "label-46")
        item.status = EvChargeAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(EvChargeAuditStatus.REQUESTED) }
    }
}
