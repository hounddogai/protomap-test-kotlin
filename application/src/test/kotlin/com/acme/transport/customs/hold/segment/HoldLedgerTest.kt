package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldLedgerTest {
    @Test
    fun `starts in new`() {
        val item = newHoldLedger("code-416", "label-86")
        assertEquals(HoldLedgerStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newHoldLedger("code-416", "label-86")
        try {
            item.moveTo(HoldLedgerStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldLedgerStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newHoldLedger("code-416", "label-86")
        item.status = HoldLedgerStatus.FAILED
        assertFails("move") { item.moveTo(HoldLedgerStatus.NEW) }
    }
}
