package com.acme.transport.lastmile.redelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRedeliveryLedgerTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        assertEquals(GlobalRedeliveryLedgerStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        try {
            item.moveTo(GlobalRedeliveryLedgerStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRedeliveryLedgerStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        item.status = GlobalRedeliveryLedgerStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalRedeliveryLedgerStatus.CREATED) }
    }
}
