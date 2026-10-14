package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultBrokerDigestTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultBrokerDigest("code-360", "title-476")
        assertEquals(DefaultBrokerDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultBrokerDigest("code-360", "title-476")
        try {
            item.moveTo(DefaultBrokerDigestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultBrokerDigestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultBrokerDigest("code-360", "title-476")
        item.status = DefaultBrokerDigestStatus.FAILED
        assertFails("move") { item.moveTo(DefaultBrokerDigestStatus.NEW) }
    }
}
