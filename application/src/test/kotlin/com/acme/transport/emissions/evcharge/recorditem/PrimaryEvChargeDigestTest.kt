package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryEvChargeDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        assertEquals(PrimaryEvChargeDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        try {
            item.moveTo(PrimaryEvChargeDigestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryEvChargeDigestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryEvChargeDigest("code-869", "name-666")
        item.status = PrimaryEvChargeDigestStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryEvChargeDigestStatus.IDLE) }
    }
}
