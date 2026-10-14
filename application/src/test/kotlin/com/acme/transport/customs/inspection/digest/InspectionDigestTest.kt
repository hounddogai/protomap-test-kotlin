package com.acme.transport.customs.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newInspectionDigest("code-298", "name-623")
        assertEquals(InspectionDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newInspectionDigest("code-298", "name-623")
        try {
            item.moveTo(InspectionDigestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionDigestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newInspectionDigest("code-298", "name-623")
        item.status = InspectionDigestStatus.CLOSED
        assertFails("move") { item.moveTo(InspectionDigestStatus.DRAFT) }
    }
}
