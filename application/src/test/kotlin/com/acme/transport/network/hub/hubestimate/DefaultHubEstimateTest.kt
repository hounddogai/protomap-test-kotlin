package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubEstimateTest {
    @Test
    fun `starts in new`() {
        val item = newDefaultHubEstimate("code-552", "label-828")
        assertEquals(DefaultHubEstimateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newDefaultHubEstimate("code-552", "label-828")
        try {
            item.moveTo(DefaultHubEstimateStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubEstimateStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newDefaultHubEstimate("code-552", "label-828")
        item.status = DefaultHubEstimateStatus.FAILED
        assertFails("move") { item.moveTo(DefaultHubEstimateStatus.NEW) }
    }
}
