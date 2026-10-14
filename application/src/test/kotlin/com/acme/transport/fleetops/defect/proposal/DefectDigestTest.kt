package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectDigestTest {
    @Test
    fun `starts in open`() {
        val item = newDefectDigest("code-983", "label-797")
        assertEquals(DefectDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDefectDigest("code-983", "label-797")
        try {
            item.moveTo(DefectDigestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectDigestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefectDigest("code-983", "label-797")
        item.status = DefectDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefectDigestStatus.OPEN) }
    }
}
