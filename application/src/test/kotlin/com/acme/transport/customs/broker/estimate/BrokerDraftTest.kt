package com.acme.transport.customs.broker.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerDraftTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerDraft("code-109", "label-310")
        assertEquals(BrokerDraftStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newBrokerDraft("code-109", "label-310")
        try {
            item.moveTo(BrokerDraftStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerDraftStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerDraft("code-109", "label-310")
        item.status = BrokerDraftStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerDraftStatus.CREATED) }
    }
}
