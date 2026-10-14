package com.acme.transport.network.transittime.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TransitTimeRollupTest {
    @Test
    fun `starts in created`() {
        val item = newTransitTimeRollup("code-359", "label-924")
        assertEquals(TransitTimeRollupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newTransitTimeRollup("code-359", "label-924")
        try {
            item.moveTo(TransitTimeRollupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TransitTimeRollupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTransitTimeRollup("code-359", "label-924")
        item.status = TransitTimeRollupStatus.RETIRED
        assertFails("move") { item.moveTo(TransitTimeRollupStatus.CREATED) }
    }
}
