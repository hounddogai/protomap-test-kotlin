package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryCoachingLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryCoachingLedger("code-47", "title-161")
        assertEquals(PrimaryCoachingLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryCoachingLedger("code-47", "title-161")
        try {
            item.moveTo(PrimaryCoachingLedgerStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryCoachingLedgerStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryCoachingLedger("code-47", "title-161")
        item.status = PrimaryCoachingLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryCoachingLedgerStatus.CREATED) }
    }
}
