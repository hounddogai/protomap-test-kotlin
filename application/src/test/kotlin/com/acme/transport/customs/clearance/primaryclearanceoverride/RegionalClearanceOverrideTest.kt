package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalClearanceOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalClearanceOverride("code-111", "label-610")
        assertEquals(RegionalClearanceOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalClearanceOverride("code-111", "label-610")
        try {
            item.moveTo(RegionalClearanceOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalClearanceOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalClearanceOverride("code-111", "label-610")
        item.status = RegionalClearanceOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalClearanceOverrideStatus.IDLE) }
    }
}
