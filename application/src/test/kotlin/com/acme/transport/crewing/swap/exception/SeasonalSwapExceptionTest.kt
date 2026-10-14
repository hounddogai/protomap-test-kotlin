package com.acme.transport.crewing.swap.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalSwapExceptionTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalSwapException("code-246", "label-911")
        assertEquals(SeasonalSwapExceptionStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newSeasonalSwapException("code-246", "label-911")
        try {
            item.moveTo(SeasonalSwapExceptionStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalSwapExceptionStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalSwapException("code-246", "label-911")
        item.status = SeasonalSwapExceptionStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalSwapExceptionStatus.PLANNED) }
    }
}
