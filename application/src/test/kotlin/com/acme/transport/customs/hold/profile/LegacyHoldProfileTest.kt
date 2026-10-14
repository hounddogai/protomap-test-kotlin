package com.acme.transport.customs.hold.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyHoldProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyHoldProfile("code-235", "name-26")
        assertEquals(LegacyHoldProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLegacyHoldProfile("code-235", "name-26")
        try {
            item.moveTo(LegacyHoldProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyHoldProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyHoldProfile("code-235", "name-26")
        item.status = LegacyHoldProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyHoldProfileStatus.DRAFT) }
    }
}
