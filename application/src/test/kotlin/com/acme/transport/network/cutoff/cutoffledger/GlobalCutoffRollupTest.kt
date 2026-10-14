package com.acme.transport.network.cutoff.cutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCutoffRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCutoffRollup("code-18", "title-713")
        assertEquals(GlobalCutoffRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalCutoffRollup("code-18", "title-713")
        try {
            item.moveTo(GlobalCutoffRollupStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCutoffRollupStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCutoffRollup("code-18", "title-713")
        item.status = GlobalCutoffRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCutoffRollupStatus.PLANNED) }
    }
}
