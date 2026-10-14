package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredRestBreakPreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredRestBreakPreference("code-504", "label-351")
        assertEquals(TieredRestBreakPreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredRestBreakPreference("code-504", "label-351")
        try {
            item.moveTo(TieredRestBreakPreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredRestBreakPreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredRestBreakPreference("code-504", "label-351")
        item.status = TieredRestBreakPreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredRestBreakPreferenceStatus.REQUESTED) }
    }
}
