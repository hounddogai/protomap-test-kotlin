package com.acme.transport.terminal.sortation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackSortationVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackSortationVariance("code-648", "title-831")
        assertEquals(FallbackSortationVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackSortationVariance("code-648", "title-831")
        try {
            item.moveTo(FallbackSortationVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackSortationVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackSortationVariance("code-648", "title-831")
        item.status = FallbackSortationVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackSortationVarianceStatus.IDLE) }
    }
}
