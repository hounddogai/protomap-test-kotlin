package com.acme.transport.network.hub.hubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubTierTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultHubTier("code-671", "title-321")
        assertEquals(DefaultHubTierStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefaultHubTier("code-671", "title-321")
        try {
            item.moveTo(DefaultHubTierStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubTierStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultHubTier("code-671", "title-321")
        item.status = DefaultHubTierStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultHubTierStatus.PROPOSED) }
    }
}
