package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingBrokerQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingBrokerQuota("code-119", "label-910")
        assertEquals(RollingBrokerQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingBrokerQuota("code-119", "label-910")
        try {
            item.moveTo(RollingBrokerQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingBrokerQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingBrokerQuota("code-119", "label-910")
        item.status = RollingBrokerQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingBrokerQuotaStatus.REQUESTED) }
    }
}
