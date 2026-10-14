package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakDigestTest {
    @Test
    fun `starts in planned`() {
        val item = newRestBreakDigest("code-236", "label-498")
        assertEquals(RestBreakDigestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newRestBreakDigest("code-236", "label-498")
        try {
            item.moveTo(RestBreakDigestStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakDigestStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRestBreakDigest("code-236", "label-498")
        item.status = RestBreakDigestStatus.CANCELLED
        assertFails("move") { item.moveTo(RestBreakDigestStatus.PLANNED) }
    }
}
