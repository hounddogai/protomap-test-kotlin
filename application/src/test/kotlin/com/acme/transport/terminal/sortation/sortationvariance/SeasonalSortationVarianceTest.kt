package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalSortationVarianceTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalSortationVariance("code-737", "label-765")
        assertEquals(SeasonalSortationVarianceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalSortationVariance("code-737", "label-765")
        try {
            item.moveTo(SeasonalSortationVarianceStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalSortationVarianceStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalSortationVariance("code-737", "label-765")
        item.status = SeasonalSortationVarianceStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalSortationVarianceStatus.REQUESTED) }
    }
}
