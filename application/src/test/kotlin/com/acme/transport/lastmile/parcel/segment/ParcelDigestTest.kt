package com.acme.transport.lastmile.parcel.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelDigestTest {
    @Test
    fun `starts in created`() {
        val item = newParcelDigest("code-556", "title-262")
        assertEquals(ParcelDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newParcelDigest("code-556", "title-262")
        try {
            item.moveTo(ParcelDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newParcelDigest("code-556", "title-262")
        item.status = ParcelDigestStatus.RETIRED
        assertFails("move") { item.moveTo(ParcelDigestStatus.CREATED) }
    }
}
