package com.acme.transport.lastmile.drop.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDropPreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultDropPreference("code-88", "title-315")
        assertEquals(DefaultDropPreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultDropPreference("code-88", "title-315")
        try {
            item.moveTo(DefaultDropPreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDropPreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultDropPreference("code-88", "title-315")
        item.status = DefaultDropPreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultDropPreferenceStatus.PROPOSED) }
    }
}
