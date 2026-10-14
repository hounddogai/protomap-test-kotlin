package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderRollup("code-933", "label-431")
        assertEquals(TenderRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newTenderRollup("code-933", "label-431")
        try {
            item.moveTo(TenderRollupStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderRollupStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderRollup("code-933", "label-431")
        item.status = TenderRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderRollupStatus.PLANNED) }
    }
}
