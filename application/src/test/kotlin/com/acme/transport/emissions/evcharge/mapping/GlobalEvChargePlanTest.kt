package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalEvChargePlanTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalEvChargePlan("code-403", "name-510")
        assertEquals(GlobalEvChargePlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalEvChargePlan("code-403", "name-510")
        try {
            item.moveTo(GlobalEvChargePlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalEvChargePlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalEvChargePlan("code-403", "name-510")
        item.status = GlobalEvChargePlanStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalEvChargePlanStatus.IDLE) }
    }
}
