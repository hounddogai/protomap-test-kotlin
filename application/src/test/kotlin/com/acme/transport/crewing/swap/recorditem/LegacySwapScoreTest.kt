package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacySwapScoreTest {
    @Test
    fun `starts in new`() {
        val item = newLegacySwapScore("code-556", "label-701")
        assertEquals(LegacySwapScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLegacySwapScore("code-556", "label-701")
        try {
            item.moveTo(LegacySwapScoreStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacySwapScoreStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacySwapScore("code-556", "label-701")
        item.status = LegacySwapScoreStatus.FAILED
        assertFails("move") { item.moveTo(LegacySwapScoreStatus.NEW) }
    }
}
