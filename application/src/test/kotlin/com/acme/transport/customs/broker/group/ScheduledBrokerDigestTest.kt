package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledBrokerDigestTest {
    @Test
    fun `starts in open`() {
        val item = newScheduledBrokerDigest("code-785", "label-418")
        assertEquals(ScheduledBrokerDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newScheduledBrokerDigest("code-785", "label-418")
        try {
            item.moveTo(ScheduledBrokerDigestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledBrokerDigestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newScheduledBrokerDigest("code-785", "label-418")
        item.status = ScheduledBrokerDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(ScheduledBrokerDigestStatus.OPEN) }
    }
}
