package com.acme.transport.terminal.sortation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newSortationEstimate("code-432", "name-444")
        assertEquals(SortationEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSortationEstimate("code-432", "name-444")
        try {
            item.moveTo(SortationEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSortationEstimate("code-432", "name-444")
        item.status = SortationEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(SortationEstimateStatus.CREATED) }
    }
}
