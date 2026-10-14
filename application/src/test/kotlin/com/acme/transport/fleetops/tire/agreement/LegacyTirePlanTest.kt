package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTirePlanTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyTirePlan("code-590", "title-325")
        assertEquals(LegacyTirePlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLegacyTirePlan("code-590", "title-325")
        try {
            item.moveTo(LegacyTirePlanStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTirePlanStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyTirePlan("code-590", "title-325")
        item.status = LegacyTirePlanStatus.FAILED
        assertFails("move") { item.moveTo(LegacyTirePlanStatus.NEW) }
    }
}
