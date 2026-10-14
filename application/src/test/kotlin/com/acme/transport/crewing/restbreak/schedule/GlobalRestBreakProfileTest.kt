package com.acme.transport.crewing.restbreak.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalRestBreakProfileTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalRestBreakProfile("code-395", "name-948")
        assertEquals(GlobalRestBreakProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newGlobalRestBreakProfile("code-395", "name-948")
        try {
            item.moveTo(GlobalRestBreakProfileStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalRestBreakProfileStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalRestBreakProfile("code-395", "name-948")
        item.status = GlobalRestBreakProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalRestBreakProfileStatus.OPEN) }
    }
}
