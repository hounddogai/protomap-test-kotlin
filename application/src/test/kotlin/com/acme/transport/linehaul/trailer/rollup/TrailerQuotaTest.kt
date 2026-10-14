package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerQuotaTest {
    @Test
    fun `starts in open`() {
        val item = newTrailerQuota("code-519", "title-868")
        assertEquals(TrailerQuotaStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTrailerQuota("code-519", "title-868")
        try {
            item.moveTo(TrailerQuotaStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerQuotaStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTrailerQuota("code-519", "title-868")
        item.status = TrailerQuotaStatus.ARCHIVED
        assertFails("move") { item.moveTo(TrailerQuotaStatus.OPEN) }
    }
}
