package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultHubPlanTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultHubPlan("code-799", "label-712")
        assertEquals(DefaultHubPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newDefaultHubPlan("code-799", "label-712")
        try {
            item.moveTo(DefaultHubPlanStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultHubPlanStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultHubPlan("code-799", "label-712")
        item.status = DefaultHubPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultHubPlanStatus.OPEN) }
    }
}
