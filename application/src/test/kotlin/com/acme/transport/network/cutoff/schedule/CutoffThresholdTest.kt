package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newCutoffThreshold("code-224", "title-481")
        assertEquals(CutoffThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCutoffThreshold("code-224", "title-481")
        try {
            item.moveTo(CutoffThresholdStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffThresholdStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCutoffThreshold("code-224", "title-481")
        item.status = CutoffThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CutoffThresholdStatus.PROPOSED) }
    }
}
