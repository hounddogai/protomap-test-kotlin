package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNodeScoreTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalNodeScore("code-173", "name-727")
        assertEquals(RegionalNodeScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalNodeScore("code-173", "name-727")
        try {
            item.moveTo(RegionalNodeScoreStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNodeScoreStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalNodeScore("code-173", "name-727")
        item.status = RegionalNodeScoreStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalNodeScoreStatus.CREATED) }
    }
}
