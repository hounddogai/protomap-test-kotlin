package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DropVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDropVariance("code-877", "name-405")
        assertEquals(DropVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newDropVariance("code-877", "name-405")
        try {
            item.moveTo(DropVarianceStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DropVarianceStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDropVariance("code-877", "name-405")
        item.status = DropVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DropVarianceStatus.PROPOSED) }
    }
}
