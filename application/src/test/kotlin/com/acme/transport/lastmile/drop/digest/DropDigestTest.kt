package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newDropDigest("code-541", "label-620")
        assertEquals(DropDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newDropDigest("code-541", "label-620")
        try {
            item.moveTo(DropDigestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropDigestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDropDigest("code-541", "label-620")
        item.status = DropDigestStatus.STOPPED
        assertFails("move") { item.moveTo(DropDigestStatus.IDLE) }
    }
}
