package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientCrossDockQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientCrossDockQuota("code-376", "name-455")
        assertEquals(LenientCrossDockQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientCrossDockQuota("code-376", "name-455")
        try {
            item.moveTo(LenientCrossDockQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientCrossDockQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientCrossDockQuota("code-376", "name-455")
        item.status = LenientCrossDockQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientCrossDockQuotaStatus.REQUESTED) }
    }
}
