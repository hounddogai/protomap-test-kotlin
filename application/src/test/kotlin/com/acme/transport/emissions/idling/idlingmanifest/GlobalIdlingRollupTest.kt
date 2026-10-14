package com.acme.transport.emissions.idling.idlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalIdlingRollupTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalIdlingRollup("code-263", "name-533")
        assertEquals(GlobalIdlingRollupStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalIdlingRollup("code-263", "name-533")
        try {
            item.moveTo(GlobalIdlingRollupStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalIdlingRollupStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalIdlingRollup("code-263", "name-533")
        item.status = GlobalIdlingRollupStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalIdlingRollupStatus.PROPOSED) }
    }
}
