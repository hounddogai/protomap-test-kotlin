package com.acme.transport.customs.clearance.clearancemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedClearanceBundleTest {
    @Test
    fun `starts in created`() {
        val item = newCappedClearanceBundle("code-297", "label-514")
        assertEquals(CappedClearanceBundleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCappedClearanceBundle("code-297", "label-514")
        try {
            item.moveTo(CappedClearanceBundleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedClearanceBundleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedClearanceBundle("code-297", "label-514")
        item.status = CappedClearanceBundleStatus.RETIRED
        assertFails("move") { item.moveTo(CappedClearanceBundleStatus.CREATED) }
    }
}
