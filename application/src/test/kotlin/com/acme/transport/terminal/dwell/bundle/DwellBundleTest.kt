package com.acme.transport.terminal.dwell.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DwellBundleTest {
    @Test
    fun `starts in planned`() {
        val item = newDwellBundle("code-783", "label-330")
        assertEquals(DwellBundleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDwellBundle("code-783", "label-330")
        try {
            item.moveTo(DwellBundleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DwellBundleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDwellBundle("code-783", "label-330")
        item.status = DwellBundleStatus.CANCELLED
        assertFails("move") { item.moveTo(DwellBundleStatus.PLANNED) }
    }
}
