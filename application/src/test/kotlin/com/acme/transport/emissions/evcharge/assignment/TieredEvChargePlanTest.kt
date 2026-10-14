package com.acme.transport.emissions.evcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredEvChargePlanTest {
    @Test
    fun `starts in created`() {
        val item = newTieredEvChargePlan("code-957", "title-506")
        assertEquals(TieredEvChargePlanStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTieredEvChargePlan("code-957", "title-506")
        try {
            item.moveTo(TieredEvChargePlanStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredEvChargePlanStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTieredEvChargePlan("code-957", "title-506")
        item.status = TieredEvChargePlanStatus.RETIRED
        assertFails("move") { item.moveTo(TieredEvChargePlanStatus.CREATED) }
    }
}
