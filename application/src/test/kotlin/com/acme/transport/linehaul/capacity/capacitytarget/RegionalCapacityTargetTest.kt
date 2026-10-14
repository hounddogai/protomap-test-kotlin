package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCapacityTargetTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalCapacityTarget("code-421", "name-721")
        assertEquals(RegionalCapacityTargetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalCapacityTarget("code-421", "name-721")
        try {
            item.moveTo(RegionalCapacityTargetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCapacityTargetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalCapacityTarget("code-421", "name-721")
        item.status = RegionalCapacityTargetStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalCapacityTargetStatus.CREATED) }
    }
}
