package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldDigestTest {
    @Test
    fun `starts in created`() {
        val item = newHoldDigest("code-146", "label-679")
        assertEquals(HoldDigestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newHoldDigest("code-146", "label-679")
        try {
            item.moveTo(HoldDigestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldDigestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHoldDigest("code-146", "label-679")
        item.status = HoldDigestStatus.RETIRED
        assertFails("move") { item.moveTo(HoldDigestStatus.CREATED) }
    }
}
