package com.acme.transport.linehaul.backhaul.backhaulthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBackhaulLedgerTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalBackhaulLedger("code-665", "name-674")
        assertEquals(RegionalBackhaulLedgerStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newRegionalBackhaulLedger("code-665", "name-674")
        try {
            item.moveTo(RegionalBackhaulLedgerStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBackhaulLedgerStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalBackhaulLedger("code-665", "name-674")
        item.status = RegionalBackhaulLedgerStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalBackhaulLedgerStatus.OPEN) }
    }
}
