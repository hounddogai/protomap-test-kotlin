package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultRosterPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newDefaultRosterPreference("code-204", "title-233")
        assertEquals(DefaultRosterPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newDefaultRosterPreference("code-204", "title-233")
        try {
            item.moveTo(DefaultRosterPreferenceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultRosterPreferenceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDefaultRosterPreference("code-204", "title-233")
        item.status = DefaultRosterPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(DefaultRosterPreferenceStatus.OPEN) }
    }
}
