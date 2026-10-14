package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalNodeBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalNodeBundle("code-392", "title-672")
        assertEquals(SeasonalNodeBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalNodeBundle("code-392", "title-672")
        try {
            item.moveTo(SeasonalNodeBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalNodeBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalNodeBundle("code-392", "title-672")
        item.status = SeasonalNodeBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalNodeBundleStatus.PROPOSED) }
    }
}
