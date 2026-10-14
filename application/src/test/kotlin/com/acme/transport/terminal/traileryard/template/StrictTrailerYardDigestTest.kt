package com.acme.transport.terminal.traileryard.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictTrailerYardDigestTest {
    @Test
    fun `starts in created`() {
        val item = newStrictTrailerYardDigest("code-633", "name-961")
        assertEquals(StrictTrailerYardDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newStrictTrailerYardDigest("code-633", "name-961")
        try {
            item.moveTo(StrictTrailerYardDigestStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictTrailerYardDigestStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictTrailerYardDigest("code-633", "name-961")
        item.status = StrictTrailerYardDigestStatus.RETIRED
        assertFails("move") { item.moveTo(StrictTrailerYardDigestStatus.CREATED) }
    }
}
