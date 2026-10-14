package com.acme.transport.emissions.evcharge.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newEvChargeDigest("code-382", "name-668")
        assertEquals(EvChargeDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newEvChargeDigest("code-382", "name-668")
        try {
            item.moveTo(EvChargeDigestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeDigestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newEvChargeDigest("code-382", "name-668")
        item.status = EvChargeDigestStatus.STOPPED
        assertFails("move") { item.moveTo(EvChargeDigestStatus.IDLE) }
    }
}
