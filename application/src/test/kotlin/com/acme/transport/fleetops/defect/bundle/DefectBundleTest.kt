package com.acme.transport.fleetops.defect.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newDefectBundle("code-454", "name-147")
        assertEquals(DefectBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newDefectBundle("code-454", "name-147")
        try {
            item.moveTo(DefectBundleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectBundleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefectBundle("code-454", "name-147")
        item.status = DefectBundleStatus.CLOSED
        assertFails("move") { item.moveTo(DefectBundleStatus.DRAFT) }
    }
}
