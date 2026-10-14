package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictCutoffRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictCutoffRollup("code-354", "label-997")
        assertEquals(StrictCutoffRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictCutoffRollup("code-354", "label-997")
        try {
            item.moveTo(StrictCutoffRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictCutoffRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictCutoffRollup("code-354", "label-997")
        item.status = StrictCutoffRollupStatus.STOPPED
        assertFails("move") { item.moveTo(StrictCutoffRollupStatus.IDLE) }
    }
}
