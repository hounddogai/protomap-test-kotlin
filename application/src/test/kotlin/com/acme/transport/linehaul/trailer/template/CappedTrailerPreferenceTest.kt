package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedTrailerPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newCappedTrailerPreference("code-538", "title-809")
        assertEquals(CappedTrailerPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newCappedTrailerPreference("code-538", "title-809")
        try {
            item.moveTo(CappedTrailerPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedTrailerPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCappedTrailerPreference("code-538", "title-809")
        item.status = CappedTrailerPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(CappedTrailerPreferenceStatus.REQUESTED) }
    }
}
