package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCutoffProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalCutoffProfile("code-331", "title-489")
        assertEquals(GlobalCutoffProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalCutoffProfile("code-331", "title-489")
        try {
            item.moveTo(GlobalCutoffProfileStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCutoffProfileStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalCutoffProfile("code-331", "title-489")
        item.status = GlobalCutoffProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalCutoffProfileStatus.PROPOSED) }
    }
}
