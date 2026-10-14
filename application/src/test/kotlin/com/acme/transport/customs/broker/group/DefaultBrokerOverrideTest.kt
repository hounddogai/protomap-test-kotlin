package com.acme.transport.customs.broker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultBrokerOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultBrokerOverride("code-778", "label-372")
        assertEquals(DefaultBrokerOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newDefaultBrokerOverride("code-778", "label-372")
        try {
            item.moveTo(DefaultBrokerOverrideStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultBrokerOverrideStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultBrokerOverride("code-778", "label-372")
        item.status = DefaultBrokerOverrideStatus.FAILED
        assertFails("move") { item.moveTo(DefaultBrokerOverrideStatus.NEW) }
    }
}
