package com.acme.transport.emissions.idling.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingDigestTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingDigest("code-992", "label-921")
        assertEquals(IdlingDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingDigest("code-992", "label-921")
        try {
            item.moveTo(IdlingDigestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingDigestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingDigest("code-992", "label-921")
        item.status = IdlingDigestStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingDigestStatus.CREATED) }
    }
}
