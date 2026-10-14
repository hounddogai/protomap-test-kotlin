package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTireEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultTireEstimate("code-888", "name-965")
        assertEquals(DefaultTireEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultTireEstimate("code-888", "name-965")
        try {
            item.moveTo(DefaultTireEstimateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTireEstimateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultTireEstimate("code-888", "name-965")
        item.status = DefaultTireEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultTireEstimateStatus.DRAFT) }
    }
}
