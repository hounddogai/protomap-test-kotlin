package com.acme.transport.crewing.restbreak.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakBaselineTest {
    @Test
    fun `starts in open`() {
        val item = newRestBreakBaseline("code-636", "name-173")
        assertEquals(RestBreakBaselineStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRestBreakBaseline("code-636", "name-173")
        try {
            item.moveTo(RestBreakBaselineStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakBaselineStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRestBreakBaseline("code-636", "name-173")
        item.status = RestBreakBaselineStatus.ARCHIVED
        assertFails("move") { item.moveTo(RestBreakBaselineStatus.OPEN) }
    }
}
