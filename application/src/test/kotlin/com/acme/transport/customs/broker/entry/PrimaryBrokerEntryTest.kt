package com.acme.transport.customs.broker.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryBrokerEntryTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryBrokerEntry("code-826", "name-161")
        assertEquals(PrimaryBrokerEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPrimaryBrokerEntry("code-826", "name-161")
        try {
            item.moveTo(PrimaryBrokerEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryBrokerEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryBrokerEntry("code-826", "name-161")
        item.status = PrimaryBrokerEntryStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryBrokerEntryStatus.CREATED) }
    }
}
