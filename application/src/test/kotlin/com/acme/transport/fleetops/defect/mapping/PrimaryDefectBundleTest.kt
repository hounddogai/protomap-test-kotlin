package com.acme.transport.fleetops.defect.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDefectBundleTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryDefectBundle("code-210", "title-70")
        assertEquals(PrimaryDefectBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPrimaryDefectBundle("code-210", "title-70")
        try {
            item.moveTo(PrimaryDefectBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDefectBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryDefectBundle("code-210", "title-70")
        item.status = PrimaryDefectBundleStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryDefectBundleStatus.NEW) }
    }
}
