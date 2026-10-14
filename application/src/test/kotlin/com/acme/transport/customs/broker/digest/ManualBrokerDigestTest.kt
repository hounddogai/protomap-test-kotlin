package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBrokerDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newManualBrokerDigest("code-658", "label-7")
        assertEquals(ManualBrokerDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newManualBrokerDigest("code-658", "label-7")
        try {
            item.moveTo(ManualBrokerDigestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBrokerDigestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualBrokerDigest("code-658", "label-7")
        item.status = ManualBrokerDigestStatus.CLOSED
        assertFails("move") { item.moveTo(ManualBrokerDigestStatus.DRAFT) }
    }
}
