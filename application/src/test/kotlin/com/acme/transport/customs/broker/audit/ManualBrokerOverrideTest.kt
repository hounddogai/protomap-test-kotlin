package com.acme.transport.customs.broker.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBrokerOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newManualBrokerOverride("code-723", "label-246")
        assertEquals(ManualBrokerOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newManualBrokerOverride("code-723", "label-246")
        try {
            item.moveTo(ManualBrokerOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBrokerOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualBrokerOverride("code-723", "label-246")
        item.status = ManualBrokerOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualBrokerOverrideStatus.REQUESTED) }
    }
}
