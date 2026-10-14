package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityRollupTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalCapacityRollup("code-101", "label-341")
        assertEquals(RegionalCapacityRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRegionalCapacityRollup("code-101", "label-341")
        try {
            item.moveTo(RegionalCapacityRollupStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityRollupStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalCapacityRollup("code-101", "label-341")
        item.status = RegionalCapacityRollupStatus.FAILED
        assertFails("move") { item.moveTo(RegionalCapacityRollupStatus.NEW) }
    }
}
