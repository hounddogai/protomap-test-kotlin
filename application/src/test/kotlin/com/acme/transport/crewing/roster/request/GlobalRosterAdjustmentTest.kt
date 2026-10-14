package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRosterAdjustmentTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalRosterAdjustment("code-704", "title-771")
        assertEquals(GlobalRosterAdjustmentStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalRosterAdjustment("code-704", "title-771")
        try {
            item.moveTo(GlobalRosterAdjustmentStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRosterAdjustmentStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalRosterAdjustment("code-704", "title-771")
        item.status = GlobalRosterAdjustmentStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalRosterAdjustmentStatus.REQUESTED) }
    }
}
