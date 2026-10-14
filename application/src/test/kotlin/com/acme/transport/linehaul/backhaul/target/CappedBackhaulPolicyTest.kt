package com.acme.transport.linehaul.backhaul.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBackhaulPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        assertEquals(CappedBackhaulPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        try {
            item.moveTo(CappedBackhaulPolicyStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBackhaulPolicyStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedBackhaulPolicy("code-425", "title-371")
        item.status = CappedBackhaulPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(CappedBackhaulPolicyStatus.DRAFT) }
    }
}
