package com.acme.transport.linehaul.backhaul.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newBackhaulPreference("code-393", "name-147")
        assertEquals(BackhaulPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newBackhaulPreference("code-393", "name-147")
        try {
            item.moveTo(BackhaulPreferenceStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulPreferenceStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBackhaulPreference("code-393", "name-147")
        item.status = BackhaulPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(BackhaulPreferenceStatus.NEW) }
    }
}
