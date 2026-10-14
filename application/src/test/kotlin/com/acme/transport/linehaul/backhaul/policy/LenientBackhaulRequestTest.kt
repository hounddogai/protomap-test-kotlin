package com.acme.transport.linehaul.backhaul.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientBackhaulRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientBackhaulRequest("code-844", "name-933")
        assertEquals(LenientBackhaulRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientBackhaulRequest("code-844", "name-933")
        try {
            item.moveTo(LenientBackhaulRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientBackhaulRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientBackhaulRequest("code-844", "name-933")
        item.status = LenientBackhaulRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientBackhaulRequestStatus.PROPOSED) }
    }
}
