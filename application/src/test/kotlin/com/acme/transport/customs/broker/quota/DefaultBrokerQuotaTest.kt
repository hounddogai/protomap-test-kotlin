package com.acme.transport.customs.broker.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultBrokerQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultBrokerQuota("code-494", "name-313")
        assertEquals(DefaultBrokerQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefaultBrokerQuota("code-494", "name-313")
        try {
            item.moveTo(DefaultBrokerQuotaStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultBrokerQuotaStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultBrokerQuota("code-494", "name-313")
        item.status = DefaultBrokerQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultBrokerQuotaStatus.REQUESTED) }
    }
}
