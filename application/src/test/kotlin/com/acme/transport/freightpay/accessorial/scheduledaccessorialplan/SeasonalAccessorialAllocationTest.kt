package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialAllocationTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalAccessorialAllocation("code-860", "label-217")
        assertEquals(SeasonalAccessorialAllocationStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newSeasonalAccessorialAllocation("code-860", "label-217")
        try {
            item.moveTo(SeasonalAccessorialAllocationStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialAllocationStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalAccessorialAllocation("code-860", "label-217")
        item.status = SeasonalAccessorialAllocationStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalAccessorialAllocationStatus.NEW) }
    }
}
