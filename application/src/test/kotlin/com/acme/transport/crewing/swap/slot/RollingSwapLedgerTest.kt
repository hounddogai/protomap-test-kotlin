package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSwapLedgerTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingSwapLedger("code-938", "label-468")
        assertEquals(RollingSwapLedgerStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingSwapLedger("code-938", "label-468")
        try {
            item.moveTo(RollingSwapLedgerStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSwapLedgerStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingSwapLedger("code-938", "label-468")
        item.status = RollingSwapLedgerStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingSwapLedgerStatus.REQUESTED) }
    }
}
