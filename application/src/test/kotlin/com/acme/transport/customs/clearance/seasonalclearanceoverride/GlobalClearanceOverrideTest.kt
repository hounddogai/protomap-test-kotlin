package com.acme.transport.customs.clearance.seasonalclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalClearanceOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newGlobalClearanceOverride("code-278", "title-430")
        assertEquals(GlobalClearanceOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newGlobalClearanceOverride("code-278", "title-430")
        try {
            item.moveTo(GlobalClearanceOverrideStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalClearanceOverrideStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newGlobalClearanceOverride("code-278", "title-430")
        item.status = GlobalClearanceOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(GlobalClearanceOverrideStatus.DRAFT) }
    }
}
