package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newAccessorialLedger("code-622", "title-610")
        assertEquals(AccessorialLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAccessorialLedger("code-622", "title-610")
        try {
            item.moveTo(AccessorialLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAccessorialLedger("code-622", "title-610")
        item.status = AccessorialLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AccessorialLedgerStatus.PROPOSED) }
    }
}
