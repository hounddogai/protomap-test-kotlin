package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDwellCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newStrictDwellCheckpoint("code-727", "label-506")
        assertEquals(StrictDwellCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newStrictDwellCheckpoint("code-727", "label-506")
        try {
            item.moveTo(StrictDwellCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDwellCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newStrictDwellCheckpoint("code-727", "label-506")
        item.status = StrictDwellCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(StrictDwellCheckpointStatus.CREATED) }
    }
}
