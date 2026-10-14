package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderScoreTest {
    @Test
    fun `starts in draft`() {
        val item = newTenderScore("code-681", "label-714")
        assertEquals(TenderScoreStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTenderScore("code-681", "label-714")
        try {
            item.moveTo(TenderScoreStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderScoreStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTenderScore("code-681", "label-714")
        item.status = TenderScoreStatus.CLOSED
        assertFails("move") { item.moveTo(TenderScoreStatus.DRAFT) }
    }
}
