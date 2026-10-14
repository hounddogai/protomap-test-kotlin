package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HubQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newHubQuota("code-511", "name-922")
        assertEquals(HubQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newHubQuota("code-511", "name-922")
        try {
            item.moveTo(HubQuotaStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HubQuotaStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHubQuota("code-511", "name-922")
        item.status = HubQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(HubQuotaStatus.REQUESTED) }
    }
}
