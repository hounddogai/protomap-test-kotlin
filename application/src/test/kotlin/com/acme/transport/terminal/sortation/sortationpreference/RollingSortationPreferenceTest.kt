package com.acme.transport.terminal.sortation.sortationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingSortationPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingSortationPreference("code-94", "title-75")
        assertEquals(RollingSortationPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingSortationPreference("code-94", "title-75")
        try {
            item.moveTo(RollingSortationPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingSortationPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingSortationPreference("code-94", "title-75")
        item.status = RollingSortationPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingSortationPreferenceStatus.PROPOSED) }
    }
}
