package com.acme.transport.lastmile.redelivery.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryDigestTest {
    @Test
    fun `starts in open`() {
        val item = newRedeliveryDigest("code-651", "title-995")
        assertEquals(RedeliveryDigestStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRedeliveryDigest("code-651", "title-995")
        try {
            item.moveTo(RedeliveryDigestStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryDigestStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRedeliveryDigest("code-651", "title-995")
        item.status = RedeliveryDigestStatus.ARCHIVED
        assertFails("move") { item.moveTo(RedeliveryDigestStatus.OPEN) }
    }
}
