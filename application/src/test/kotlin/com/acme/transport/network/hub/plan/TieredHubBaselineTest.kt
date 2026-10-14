package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredHubBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredHubBaseline("code-730", "name-26")
        assertEquals(TieredHubBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredHubBaseline("code-730", "name-26")
        try {
            item.moveTo(TieredHubBaselineStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredHubBaselineStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredHubBaseline("code-730", "name-26")
        item.status = TieredHubBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredHubBaselineStatus.PROPOSED) }
    }
}
