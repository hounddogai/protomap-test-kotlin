package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveIdlingDigestTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveIdlingDigest("code-776", "title-174")
        assertEquals(AdaptiveIdlingDigestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveIdlingDigest("code-776", "title-174")
        try {
            item.moveTo(AdaptiveIdlingDigestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveIdlingDigestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveIdlingDigest("code-776", "title-174")
        item.status = AdaptiveIdlingDigestStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveIdlingDigestStatus.DRAFT) }
    }
}
