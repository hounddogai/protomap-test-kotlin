package com.acme.transport.linehaul.trailer.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newTrailerPolicy("code-729", "label-134")
        assertEquals(TrailerPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTrailerPolicy("code-729", "label-134")
        try {
            item.moveTo(TrailerPolicyStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerPolicyStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTrailerPolicy("code-729", "label-134")
        item.status = TrailerPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(TrailerPolicyStatus.REQUESTED) }
    }
}
