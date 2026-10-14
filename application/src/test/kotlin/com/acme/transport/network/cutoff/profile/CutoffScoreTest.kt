package com.acme.transport.network.cutoff.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newCutoffScore("code-614", "title-923")
        assertEquals(CutoffScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCutoffScore("code-614", "title-923")
        try {
            item.moveTo(CutoffScoreStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffScoreStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCutoffScore("code-614", "title-923")
        item.status = CutoffScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CutoffScoreStatus.PROPOSED) }
    }
}
