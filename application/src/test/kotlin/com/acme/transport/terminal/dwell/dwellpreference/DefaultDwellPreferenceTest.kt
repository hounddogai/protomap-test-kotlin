package com.acme.transport.terminal.dwell.dwellpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDwellPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultDwellPreference("code-276", "name-114")
        assertEquals(DefaultDwellPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultDwellPreference("code-276", "name-114")
        try {
            item.moveTo(DefaultDwellPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDwellPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultDwellPreference("code-276", "name-114")
        item.status = DefaultDwellPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultDwellPreferenceStatus.REQUESTED) }
    }
}
