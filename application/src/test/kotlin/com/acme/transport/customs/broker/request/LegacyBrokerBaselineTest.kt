package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBrokerBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyBrokerBaseline("code-967", "name-29")
        assertEquals(LegacyBrokerBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLegacyBrokerBaseline("code-967", "name-29")
        try {
            item.moveTo(LegacyBrokerBaselineStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBrokerBaselineStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyBrokerBaseline("code-967", "name-29")
        item.status = LegacyBrokerBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyBrokerBaselineStatus.REQUESTED) }
    }
}
