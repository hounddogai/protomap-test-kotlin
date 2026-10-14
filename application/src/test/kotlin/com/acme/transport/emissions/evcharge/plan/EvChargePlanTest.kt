package com.acme.transport.emissions.evcharge.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargePlanTest {
    @Test
    fun `starts in new`() {
        val item = newEvChargePlan("code-515", "title-634")
        assertEquals(EvChargePlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newEvChargePlan("code-515", "title-634")
        try {
            item.moveTo(EvChargePlanStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargePlanStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newEvChargePlan("code-515", "title-634")
        item.status = EvChargePlanStatus.FAILED
        assertFails("move") { item.moveTo(EvChargePlanStatus.NEW) }
    }
}
