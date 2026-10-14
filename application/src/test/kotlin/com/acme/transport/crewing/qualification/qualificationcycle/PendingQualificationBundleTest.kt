package com.acme.transport.crewing.qualification.qualificationcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingQualificationBundleTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingQualificationBundle("code-390", "name-642")
        assertEquals(PendingQualificationBundleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPendingQualificationBundle("code-390", "name-642")
        try {
            item.moveTo(PendingQualificationBundleStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingQualificationBundleStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingQualificationBundle("code-390", "name-642")
        item.status = PendingQualificationBundleStatus.STOPPED
        assertFails("move") { item.moveTo(PendingQualificationBundleStatus.IDLE) }
    }
}
