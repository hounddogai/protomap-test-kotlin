package com.acme.transport.network.zone.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredZonePlanTest {
    @Test
    fun `starts in new`() {
        val item = newTieredZonePlan("code-194", "title-481")
        assertEquals(TieredZonePlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredZonePlan("code-194", "title-481")
        try {
            item.moveTo(TieredZonePlanStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredZonePlanStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredZonePlan("code-194", "title-481")
        item.status = TieredZonePlanStatus.FAILED
        assertFails("move") { item.moveTo(TieredZonePlanStatus.NEW) }
    }
}
