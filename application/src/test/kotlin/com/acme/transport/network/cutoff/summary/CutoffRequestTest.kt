package com.acme.transport.network.cutoff.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffRequestTest {
    @Test
    fun `starts in open`() {
        val item = newCutoffRequest("code-864", "title-174")
        assertEquals(CutoffRequestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newCutoffRequest("code-864", "title-174")
        try {
            item.moveTo(CutoffRequestStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffRequestStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newCutoffRequest("code-864", "title-174")
        item.status = CutoffRequestStatus.ARCHIVED
        assertFails("move") { item.moveTo(CutoffRequestStatus.OPEN) }
    }
}
