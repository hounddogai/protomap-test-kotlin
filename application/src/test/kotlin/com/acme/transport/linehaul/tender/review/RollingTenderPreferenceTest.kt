package com.acme.transport.linehaul.tender.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTenderPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newRollingTenderPreference("code-618", "name-843")
        assertEquals(RollingTenderPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRollingTenderPreference("code-618", "name-843")
        try {
            item.moveTo(RollingTenderPreferenceStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTenderPreferenceStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRollingTenderPreference("code-618", "name-843")
        item.status = RollingTenderPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(RollingTenderPreferenceStatus.REQUESTED) }
    }
}
