package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRedeliveryBundleTest {
    @Test
    fun `starts in new`() {
        val item = newTieredRedeliveryBundle("code-168", "name-344")
        assertEquals(TieredRedeliveryBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredRedeliveryBundle("code-168", "name-344")
        try {
            item.moveTo(TieredRedeliveryBundleStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRedeliveryBundleStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredRedeliveryBundle("code-168", "name-344")
        item.status = TieredRedeliveryBundleStatus.FAILED
        assertFails("move") { item.moveTo(TieredRedeliveryBundleStatus.NEW) }
    }
}
