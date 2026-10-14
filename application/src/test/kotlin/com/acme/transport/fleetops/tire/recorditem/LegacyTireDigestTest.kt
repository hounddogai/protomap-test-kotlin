package com.acme.transport.fleetops.tire.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyTireDigestTest {
    @Test
    fun `starts in open`() {
        val item = newLegacyTireDigest("code-640", "label-692")
        assertEquals(LegacyTireDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newLegacyTireDigest("code-640", "label-692")
        try {
            item.moveTo(LegacyTireDigestStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyTireDigestStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLegacyTireDigest("code-640", "label-692")
        item.status = LegacyTireDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(LegacyTireDigestStatus.OPEN) }
    }
}
