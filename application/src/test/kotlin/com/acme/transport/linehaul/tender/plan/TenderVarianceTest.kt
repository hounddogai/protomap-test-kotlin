package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TenderVarianceTest {
    @Test
    fun `starts in planned`() {
        val item = newTenderVariance("code-800", "title-429")
        assertEquals(TenderVarianceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newTenderVariance("code-800", "title-429")
        try {
            item.moveTo(TenderVarianceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TenderVarianceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newTenderVariance("code-800", "title-429")
        item.status = TenderVarianceStatus.CANCELLED
        assertFails("move") { item.moveTo(TenderVarianceStatus.PLANNED) }
    }
}
