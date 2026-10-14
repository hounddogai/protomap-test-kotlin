package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedParcelDigestTest {
    @Test
    fun `starts in open`() {
        val item = newCappedParcelDigest("code-100", "title-881")
        assertEquals(CappedParcelDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newCappedParcelDigest("code-100", "title-881")
        try {
            item.moveTo(CappedParcelDigestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedParcelDigestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCappedParcelDigest("code-100", "title-881")
        item.status = CappedParcelDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CappedParcelDigestStatus.OPEN) }
    }
}
