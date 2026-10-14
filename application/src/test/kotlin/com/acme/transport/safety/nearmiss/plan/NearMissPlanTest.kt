package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissPlanTest {
    @Test
    fun `starts in planned`() {
        val item = newNearMissPlan("code-764", "label-89")
        assertEquals(NearMissPlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newNearMissPlan("code-764", "label-89")
        try {
            item.moveTo(NearMissPlanStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissPlanStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newNearMissPlan("code-764", "label-89")
        item.status = NearMissPlanStatus.CANCELLED
        assertFails("move") { item.moveTo(NearMissPlanStatus.PLANNED) }
    }
}
