package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingAccessorialOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newPendingAccessorialOverride("code-647", "title-681")
        assertEquals(PendingAccessorialOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newPendingAccessorialOverride("code-647", "title-681")
        try {
            item.moveTo(PendingAccessorialOverrideStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingAccessorialOverrideStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPendingAccessorialOverride("code-647", "title-681")
        item.status = PendingAccessorialOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(PendingAccessorialOverrideStatus.DRAFT) }
    }
}
