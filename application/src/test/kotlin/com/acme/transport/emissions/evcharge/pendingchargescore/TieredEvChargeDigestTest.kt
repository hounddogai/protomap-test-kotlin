package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEvChargeDigestTest {
    @Test
    fun `starts in open`() {
        val item = newTieredEvChargeDigest("code-193", "name-549")
        assertEquals(TieredEvChargeDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newTieredEvChargeDigest("code-193", "name-549")
        try {
            item.moveTo(TieredEvChargeDigestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEvChargeDigestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredEvChargeDigest("code-193", "name-549")
        item.status = TieredEvChargeDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredEvChargeDigestStatus.OPEN) }
    }
}
