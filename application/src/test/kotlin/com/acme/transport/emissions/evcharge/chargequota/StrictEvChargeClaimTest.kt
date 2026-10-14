package com.acme.transport.emissions.evcharge.chargequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictEvChargeClaimTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictEvChargeClaim("code-101", "name-177")
        assertEquals(StrictEvChargeClaimStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newStrictEvChargeClaim("code-101", "name-177")
        try {
            item.moveTo(StrictEvChargeClaimStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictEvChargeClaimStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictEvChargeClaim("code-101", "name-177")
        item.status = StrictEvChargeClaimStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictEvChargeClaimStatus.PLANNED) }
    }
}
