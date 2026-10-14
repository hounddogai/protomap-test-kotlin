package com.acme.transport.customs.hold.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldBundleTest {
    @Test
    fun `starts in draft`() {
        val item = newHoldBundle("code-270", "title-905")
        assertEquals(HoldBundleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newHoldBundle("code-270", "title-905")
        try {
            item.moveTo(HoldBundleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldBundleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newHoldBundle("code-270", "title-905")
        item.status = HoldBundleStatus.CLOSED
        assertFails("move") { item.moveTo(HoldBundleStatus.DRAFT) }
    }
}
