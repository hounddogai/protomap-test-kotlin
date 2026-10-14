package com.acme.transport.customs.clearance.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredClearancePreferenceTest {
    @Test
    fun `starts in proposed`() {
        val item = newTieredClearancePreference("code-554", "label-775")
        assertEquals(TieredClearancePreferenceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTieredClearancePreference("code-554", "label-775")
        try {
            item.moveTo(TieredClearancePreferenceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredClearancePreferenceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTieredClearancePreference("code-554", "label-775")
        item.status = TieredClearancePreferenceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TieredClearancePreferenceStatus.PROPOSED) }
    }
}
