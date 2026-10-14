package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredNodeEstimateTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredNodeEstimate("code-529", "title-170")
        assertEquals(TieredNodeEstimateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newTieredNodeEstimate("code-529", "title-170")
        try {
            item.moveTo(TieredNodeEstimateStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredNodeEstimateStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredNodeEstimate("code-529", "title-170")
        item.status = TieredNodeEstimateStatus.CLOSED
        assertFails("move") { item.moveTo(TieredNodeEstimateStatus.DRAFT) }
    }
}
