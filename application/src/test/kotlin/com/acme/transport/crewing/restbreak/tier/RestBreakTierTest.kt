package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakTierTest {
    @Test
    fun `starts in idle`() {
        val item = newRestBreakTier("code-727", "name-693")
        assertEquals(RestBreakTierStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newRestBreakTier("code-727", "name-693")
        try {
            item.moveTo(RestBreakTierStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakTierStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRestBreakTier("code-727", "name-693")
        item.status = RestBreakTierStatus.STOPPED
        assertFails("move") { item.moveTo(RestBreakTierStatus.IDLE) }
    }
}
