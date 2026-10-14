package com.acme.transport.customs.hold.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldBaselineTest {
    @Test
    fun `starts in requested`() {
        val item = newHoldBaseline("code-710", "title-565")
        assertEquals(HoldBaselineStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newHoldBaseline("code-710", "title-565")
        try {
            item.moveTo(HoldBaselineStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldBaselineStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newHoldBaseline("code-710", "title-565")
        item.status = HoldBaselineStatus.EXPIRED
        assertFails("move") { item.moveTo(HoldBaselineStatus.REQUESTED) }
    }
}
