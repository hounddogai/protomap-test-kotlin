package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRosterLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newLenientRosterLedger("code-448", "name-991")
        assertEquals(LenientRosterLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newLenientRosterLedger("code-448", "name-991")
        try {
            item.moveTo(LenientRosterLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRosterLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLenientRosterLedger("code-448", "name-991")
        item.status = LenientRosterLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(LenientRosterLedgerStatus.CREATED) }
    }
}
