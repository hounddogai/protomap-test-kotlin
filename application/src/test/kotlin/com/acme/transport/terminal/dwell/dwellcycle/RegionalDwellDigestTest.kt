package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDwellDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalDwellDigest("code-577", "label-408")
        assertEquals(RegionalDwellDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalDwellDigest("code-577", "label-408")
        try {
            item.moveTo(RegionalDwellDigestStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDwellDigestStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalDwellDigest("code-577", "label-408")
        item.status = RegionalDwellDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalDwellDigestStatus.REQUESTED) }
    }
}
