package com.acme.transport.safety.violation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryViolationPreferenceTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryViolationPreference("code-832", "title-534")
        assertEquals(PrimaryViolationPreferenceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newPrimaryViolationPreference("code-832", "title-534")
        try {
            item.moveTo(PrimaryViolationPreferenceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryViolationPreferenceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryViolationPreference("code-832", "title-534")
        item.status = PrimaryViolationPreferenceStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryViolationPreferenceStatus.NEW) }
    }
}
