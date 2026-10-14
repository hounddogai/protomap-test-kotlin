package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerOverrideTest {
    @Test
    fun `starts in proposed`() {
        val item = newTrailerOverride("code-205", "title-277")
        assertEquals(TrailerOverrideStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newTrailerOverride("code-205", "title-277")
        try {
            item.moveTo(TrailerOverrideStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerOverrideStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTrailerOverride("code-205", "title-277")
        item.status = TrailerOverrideStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TrailerOverrideStatus.PROPOSED) }
    }
}
