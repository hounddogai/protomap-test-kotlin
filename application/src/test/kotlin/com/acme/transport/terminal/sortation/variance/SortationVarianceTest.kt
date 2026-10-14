package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationVariance("code-651", "name-876")
        assertEquals(SortationVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSortationVariance("code-651", "name-876")
        try {
            item.moveTo(SortationVarianceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationVarianceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationVariance("code-651", "name-876")
        item.status = SortationVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationVarianceStatus.REQUESTED) }
    }
}
