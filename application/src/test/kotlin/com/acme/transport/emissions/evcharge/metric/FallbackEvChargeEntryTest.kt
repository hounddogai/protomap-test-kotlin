package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackEvChargeEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        assertEquals(FallbackEvChargeEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        try {
            item.moveTo(FallbackEvChargeEntryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackEvChargeEntryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newFallbackEvChargeEntry("code-64", "label-809")
        item.status = FallbackEvChargeEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(FallbackEvChargeEntryStatus.REQUESTED) }
    }
}
