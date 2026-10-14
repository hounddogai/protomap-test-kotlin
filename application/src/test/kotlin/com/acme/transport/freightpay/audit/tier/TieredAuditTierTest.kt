package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredAuditTierTest {
    @Test
    fun `starts in planned`() {
        val item = newTieredAuditTier("code-907", "name-875")
        assertEquals(TieredAuditTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTieredAuditTier("code-907", "name-875")
        try {
            item.moveTo(TieredAuditTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredAuditTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTieredAuditTier("code-907", "name-875")
        item.status = TieredAuditTierStatus.CANCELLED
        assertFails("move") { item.moveTo(TieredAuditTierStatus.PLANNED) }
    }
}
