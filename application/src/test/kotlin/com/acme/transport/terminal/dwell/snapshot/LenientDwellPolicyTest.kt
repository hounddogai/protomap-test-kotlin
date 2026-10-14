package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDwellPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientDwellPolicy("code-938", "name-767")
        assertEquals(LenientDwellPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLenientDwellPolicy("code-938", "name-767")
        try {
            item.moveTo(LenientDwellPolicyStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDwellPolicyStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientDwellPolicy("code-938", "name-767")
        item.status = LenientDwellPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientDwellPolicyStatus.REQUESTED) }
    }
}
