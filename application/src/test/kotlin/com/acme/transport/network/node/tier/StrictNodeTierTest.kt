package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNodeTierTest {
    @Test
    fun `starts in planned`() {
        val item = newStrictNodeTier("code-949", "title-280")
        assertEquals(StrictNodeTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newStrictNodeTier("code-949", "title-280")
        try {
            item.moveTo(StrictNodeTierStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNodeTierStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newStrictNodeTier("code-949", "title-280")
        item.status = StrictNodeTierStatus.CANCELLED
        assertFails("move") { item.moveTo(StrictNodeTierStatus.PLANNED) }
    }
}
