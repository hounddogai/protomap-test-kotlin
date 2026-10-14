package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerCycleTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalTrailerCycle("code-374", "name-567")
        assertEquals(GlobalTrailerCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newGlobalTrailerCycle("code-374", "name-567")
        try {
            item.moveTo(GlobalTrailerCycleStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerCycleStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalTrailerCycle("code-374", "name-567")
        item.status = GlobalTrailerCycleStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalTrailerCycleStatus.CREATED) }
    }
}
