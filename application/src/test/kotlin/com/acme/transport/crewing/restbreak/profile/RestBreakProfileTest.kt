package com.acme.transport.crewing.restbreak.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakProfileTest {
    @Test
    fun `starts in open`() {
        val item = newRestBreakProfile("code-838", "name-947")
        assertEquals(RestBreakProfileStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRestBreakProfile("code-838", "name-947")
        try {
            item.moveTo(RestBreakProfileStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakProfileStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRestBreakProfile("code-838", "name-947")
        item.status = RestBreakProfileStatus.ARCHIVED
        assertFails("move") { item.moveTo(RestBreakProfileStatus.OPEN) }
    }
}
