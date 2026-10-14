package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellAdjustmentTest {
    @Test
    fun `starts in created`() {
        val item = newDwellAdjustment("code-470", "name-78")
        assertEquals(DwellAdjustmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDwellAdjustment("code-470", "name-78")
        try {
            item.moveTo(DwellAdjustmentStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellAdjustmentStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDwellAdjustment("code-470", "name-78")
        item.status = DwellAdjustmentStatus.RETIRED
        assertFails("move") { item.moveTo(DwellAdjustmentStatus.CREATED) }
    }
}
