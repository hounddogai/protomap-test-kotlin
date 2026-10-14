package com.acme.transport.network.node.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingNodeScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingNodeScore("code-167", "title-796")
        assertEquals(RollingNodeScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRollingNodeScore("code-167", "title-796")
        try {
            item.moveTo(RollingNodeScoreStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingNodeScoreStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingNodeScore("code-167", "title-796")
        item.status = RollingNodeScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingNodeScoreStatus.REQUESTED) }
    }
}
