package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderCycleTest {
    @Test
    fun `starts in created`() {
        val item = newTenderCycle("code-547", "title-540")
        assertEquals(TenderCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTenderCycle("code-547", "title-540")
        try {
            item.moveTo(TenderCycleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderCycleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTenderCycle("code-547", "title-540")
        item.status = TenderCycleStatus.RETIRED
        assertFails("move") { item.moveTo(TenderCycleStatus.CREATED) }
    }
}
