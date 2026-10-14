package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBrokerQuotaTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualBrokerQuota("code-864", "name-293")
        assertEquals(ManualBrokerQuotaStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualBrokerQuota("code-864", "name-293")
        try {
            item.moveTo(ManualBrokerQuotaStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBrokerQuotaStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualBrokerQuota("code-864", "name-293")
        item.status = ManualBrokerQuotaStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualBrokerQuotaStatus.PROPOSED) }
    }
}
