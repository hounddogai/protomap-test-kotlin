package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBrokerOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalBrokerOverride("code-376", "title-431")
        assertEquals(GlobalBrokerOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newGlobalBrokerOverride("code-376", "title-431")
        try {
            item.moveTo(GlobalBrokerOverrideStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBrokerOverrideStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalBrokerOverride("code-376", "title-431")
        item.status = GlobalBrokerOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalBrokerOverrideStatus.PROPOSED) }
    }
}
