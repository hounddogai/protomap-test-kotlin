package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackDropDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackDropDigest("code-596", "label-489")
        assertEquals(FallbackDropDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newFallbackDropDigest("code-596", "label-489")
        try {
            item.moveTo(FallbackDropDigestStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackDropDigestStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackDropDigest("code-596", "label-489")
        item.status = FallbackDropDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackDropDigestStatus.PLANNED) }
    }
}
