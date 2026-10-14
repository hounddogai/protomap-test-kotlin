package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredUtilizationDigestTest {
    @Test
    fun `starts in new`() {
        val item = newTieredUtilizationDigest("code-340", "name-889")
        assertEquals(TieredUtilizationDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTieredUtilizationDigest("code-340", "name-889")
        try {
            item.moveTo(TieredUtilizationDigestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredUtilizationDigestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredUtilizationDigest("code-340", "name-889")
        item.status = TieredUtilizationDigestStatus.FAILED
        assertFails("move") { item.moveTo(TieredUtilizationDigestStatus.NEW) }
    }
}
