package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBackhaulLedgerTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveBackhaulLedger("code-400", "title-115")
        assertEquals(AdaptiveBackhaulLedgerStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveBackhaulLedger("code-400", "title-115")
        try {
            item.moveTo(AdaptiveBackhaulLedgerStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBackhaulLedgerStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveBackhaulLedger("code-400", "title-115")
        item.status = AdaptiveBackhaulLedgerStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveBackhaulLedgerStatus.IDLE) }
    }
}
