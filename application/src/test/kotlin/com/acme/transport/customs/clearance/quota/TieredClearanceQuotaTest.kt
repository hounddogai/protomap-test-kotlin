package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredClearanceQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newTieredClearanceQuota("code-366", "name-390")
        assertEquals(TieredClearanceQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newTieredClearanceQuota("code-366", "name-390")
        try {
            item.moveTo(TieredClearanceQuotaStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredClearanceQuotaStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredClearanceQuota("code-366", "name-390")
        item.status = TieredClearanceQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredClearanceQuotaStatus.OPEN) }
    }
}
