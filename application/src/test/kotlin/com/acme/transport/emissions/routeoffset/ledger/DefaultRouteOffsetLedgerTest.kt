package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRouteOffsetLedgerTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultRouteOffsetLedger("code-427", "name-915")
        assertEquals(DefaultRouteOffsetLedgerStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newDefaultRouteOffsetLedger("code-427", "name-915")
        try {
            item.moveTo(DefaultRouteOffsetLedgerStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRouteOffsetLedgerStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultRouteOffsetLedger("code-427", "name-915")
        item.status = DefaultRouteOffsetLedgerStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultRouteOffsetLedgerStatus.PLANNED) }
    }
}
