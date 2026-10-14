package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultNearMissLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultNearMissLedger("code-504", "title-554")
        assertEquals(DefaultNearMissLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultNearMissLedger("code-504", "title-554")
        try {
            item.moveTo(DefaultNearMissLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultNearMissLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultNearMissLedger("code-504", "title-554")
        item.status = DefaultNearMissLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultNearMissLedgerStatus.CREATED) }
    }
}
