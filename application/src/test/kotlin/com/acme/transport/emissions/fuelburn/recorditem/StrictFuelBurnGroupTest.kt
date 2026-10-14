package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictFuelBurnGroupTest {
    @Test
    fun `starts in new`() {
        val item = newStrictFuelBurnGroup("code-174", "label-353")
        assertEquals(StrictFuelBurnGroupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newStrictFuelBurnGroup("code-174", "label-353")
        try {
            item.moveTo(StrictFuelBurnGroupStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictFuelBurnGroupStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictFuelBurnGroup("code-174", "label-353")
        item.status = StrictFuelBurnGroupStatus.FAILED
        assertFails("move") { item.moveTo(StrictFuelBurnGroupStatus.NEW) }
    }
}
