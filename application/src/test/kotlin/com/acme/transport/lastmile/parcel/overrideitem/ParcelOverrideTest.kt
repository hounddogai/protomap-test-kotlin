package com.acme.transport.lastmile.parcel.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelOverrideTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelOverride("code-481", "title-384")
        assertEquals(ParcelOverrideStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newParcelOverride("code-481", "title-384")
        try {
            item.moveTo(ParcelOverrideStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelOverrideStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelOverride("code-481", "title-384")
        item.status = ParcelOverrideStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelOverrideStatus.REQUESTED) }
    }
}
