package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTransitTimeQuotaTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultTransitTimeQuota("code-654", "name-113")
        assertEquals(DefaultTransitTimeQuotaStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newDefaultTransitTimeQuota("code-654", "name-113")
        try {
            item.moveTo(DefaultTransitTimeQuotaStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTransitTimeQuotaStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultTransitTimeQuota("code-654", "name-113")
        item.status = DefaultTransitTimeQuotaStatus.FAILED
        assertFails("move") { item.moveTo(DefaultTransitTimeQuotaStatus.NEW) }
    }
}
