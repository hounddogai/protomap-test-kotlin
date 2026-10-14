package com.acme.transport.safety.violation.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyViolationBundleTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyViolationBundle("code-965", "label-560")
        assertEquals(LegacyViolationBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newLegacyViolationBundle("code-965", "label-560")
        try {
            item.moveTo(LegacyViolationBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyViolationBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyViolationBundle("code-965", "label-560")
        item.status = LegacyViolationBundleStatus.FAILED
        assertFails("move") { item.moveTo(LegacyViolationBundleStatus.NEW) }
    }
}
