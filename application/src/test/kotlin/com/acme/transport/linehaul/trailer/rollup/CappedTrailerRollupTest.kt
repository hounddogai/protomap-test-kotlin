package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerRollupTest {
    @Test
    fun `starts in new`() {
        val item = newCappedTrailerRollup("code-580", "name-172")
        assertEquals(CappedTrailerRollupStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newCappedTrailerRollup("code-580", "name-172")
        try {
            item.moveTo(CappedTrailerRollupStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerRollupStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCappedTrailerRollup("code-580", "name-172")
        item.status = CappedTrailerRollupStatus.FAILED
        assertFails("move") { item.moveTo(CappedTrailerRollupStatus.NEW) }
    }
}
