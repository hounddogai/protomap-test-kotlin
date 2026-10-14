package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newDisputeDigest("code-540", "title-579")
        assertEquals(DisputeDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDisputeDigest("code-540", "title-579")
        try {
            item.moveTo(DisputeDigestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeDigestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDisputeDigest("code-540", "title-579")
        item.status = DisputeDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(DisputeDigestStatus.REQUESTED) }
    }
}
