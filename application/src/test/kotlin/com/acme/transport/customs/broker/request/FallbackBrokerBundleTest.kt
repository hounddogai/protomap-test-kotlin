package com.acme.transport.customs.broker.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackBrokerBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackBrokerBundle("code-180", "name-432")
        assertEquals(FallbackBrokerBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newFallbackBrokerBundle("code-180", "name-432")
        try {
            item.moveTo(FallbackBrokerBundleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackBrokerBundleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackBrokerBundle("code-180", "name-432")
        item.status = FallbackBrokerBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackBrokerBundleStatus.PROPOSED) }
    }
}
