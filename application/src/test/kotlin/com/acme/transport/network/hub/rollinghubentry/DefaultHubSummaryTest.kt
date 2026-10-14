package com.acme.transport.network.hub.rollinghubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultHubSummary("code-645", "name-410")
        assertEquals(DefaultHubSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultHubSummary("code-645", "name-410")
        try {
            item.moveTo(DefaultHubSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultHubSummary("code-645", "name-410")
        item.status = DefaultHubSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultHubSummaryStatus.CREATED) }
    }
}
