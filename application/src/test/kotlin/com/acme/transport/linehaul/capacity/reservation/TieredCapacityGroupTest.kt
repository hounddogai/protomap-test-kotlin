package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCapacityGroupTest {
    @Test
    fun `starts in new`() {
        val item = newTieredCapacityGroup("code-985", "name-214")
        assertEquals(TieredCapacityGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newTieredCapacityGroup("code-985", "name-214")
        try {
            item.moveTo(TieredCapacityGroupStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCapacityGroupStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredCapacityGroup("code-985", "name-214")
        item.status = TieredCapacityGroupStatus.FAILED
        assertFails("move") { item.moveTo(TieredCapacityGroupStatus.NEW) }
    }
}
