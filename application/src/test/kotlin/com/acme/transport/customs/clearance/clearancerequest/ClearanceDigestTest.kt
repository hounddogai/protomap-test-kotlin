package com.acme.transport.customs.clearance.clearancerequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceDigestTest {
    @Test
    fun `starts in open`() {
        val item = newClearanceDigest("code-329", "title-667")
        assertEquals(ClearanceDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newClearanceDigest("code-329", "title-667")
        try {
            item.moveTo(ClearanceDigestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceDigestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newClearanceDigest("code-329", "title-667")
        item.status = ClearanceDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(ClearanceDigestStatus.OPEN) }
    }
}
