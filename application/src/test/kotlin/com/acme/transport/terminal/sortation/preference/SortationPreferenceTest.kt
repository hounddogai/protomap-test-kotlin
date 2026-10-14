package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SortationPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newSortationPreference("code-859", "name-121")
        assertEquals(SortationPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSortationPreference("code-859", "name-121")
        try {
            item.moveTo(SortationPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SortationPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSortationPreference("code-859", "name-121")
        item.status = SortationPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(SortationPreferenceStatus.REQUESTED) }
    }
}
