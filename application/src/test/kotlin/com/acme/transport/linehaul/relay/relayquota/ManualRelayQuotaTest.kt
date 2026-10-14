package com.acme.transport.linehaul.relay.relayquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRelayQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newManualRelayQuota("code-125", "title-791")
        assertEquals(ManualRelayQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualRelayQuota("code-125", "title-791")
        try {
            item.moveTo(ManualRelayQuotaStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRelayQuotaStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualRelayQuota("code-125", "title-791")
        item.status = ManualRelayQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualRelayQuotaStatus.REQUESTED) }
    }
}
