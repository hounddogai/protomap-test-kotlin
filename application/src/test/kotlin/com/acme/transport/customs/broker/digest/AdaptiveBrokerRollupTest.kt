package com.acme.transport.customs.broker.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveBrokerRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveBrokerRollup("code-611", "name-692")
        assertEquals(AdaptiveBrokerRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newAdaptiveBrokerRollup("code-611", "name-692")
        try {
            item.moveTo(AdaptiveBrokerRollupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveBrokerRollupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveBrokerRollup("code-611", "name-692")
        item.status = AdaptiveBrokerRollupStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveBrokerRollupStatus.DRAFT) }
    }
}
