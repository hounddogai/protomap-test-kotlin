package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackNodeEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newFallbackNodeEstimate("code-611", "name-632")
        assertEquals(FallbackNodeEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newFallbackNodeEstimate("code-611", "name-632")
        try {
            item.moveTo(FallbackNodeEstimateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackNodeEstimateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFallbackNodeEstimate("code-611", "name-632")
        item.status = FallbackNodeEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(FallbackNodeEstimateStatus.CREATED) }
    }
}
