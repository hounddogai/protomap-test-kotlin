package com.acme.transport.fleetops.tire.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TirePlanTest {
    @Test
    fun `starts in open`() {
        val item = newTirePlan("code-92", "label-10")
        assertEquals(TirePlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTirePlan("code-92", "label-10")
        try {
            item.moveTo(TirePlanStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TirePlanStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTirePlan("code-92", "label-10")
        item.status = TirePlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(TirePlanStatus.OPEN) }
    }
}
