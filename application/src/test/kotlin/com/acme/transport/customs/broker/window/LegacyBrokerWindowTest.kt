package com.acme.transport.customs.broker.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyBrokerWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyBrokerWindow("code-814", "name-636")
        assertEquals(LegacyBrokerWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newLegacyBrokerWindow("code-814", "name-636")
        try {
            item.moveTo(LegacyBrokerWindowStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyBrokerWindowStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyBrokerWindow("code-814", "name-636")
        item.status = LegacyBrokerWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyBrokerWindowStatus.PROPOSED) }
    }
}
