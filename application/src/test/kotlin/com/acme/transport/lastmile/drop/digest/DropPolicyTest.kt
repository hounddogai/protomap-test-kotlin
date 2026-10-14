package com.acme.transport.lastmile.drop.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newDropPolicy("code-252", "title-846")
        assertEquals(DropPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDropPolicy("code-252", "title-846")
        try {
            item.moveTo(DropPolicyStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropPolicyStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDropPolicy("code-252", "title-846")
        item.status = DropPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(DropPolicyStatus.REQUESTED) }
    }
}
