package com.acme.transport.safety.nearmiss.misscycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictNearMissPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newStrictNearMissPolicy("code-9", "label-104")
        assertEquals(StrictNearMissPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newStrictNearMissPolicy("code-9", "label-104")
        try {
            item.moveTo(StrictNearMissPolicyStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictNearMissPolicyStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newStrictNearMissPolicy("code-9", "label-104")
        item.status = StrictNearMissPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(StrictNearMissPolicyStatus.OPEN) }
    }
}
