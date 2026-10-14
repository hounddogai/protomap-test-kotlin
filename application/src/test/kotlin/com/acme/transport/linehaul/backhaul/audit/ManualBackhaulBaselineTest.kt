package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualBackhaulBaselineTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualBackhaulBaseline("code-509", "label-586")
        assertEquals(ManualBackhaulBaselineStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualBackhaulBaseline("code-509", "label-586")
        try {
            item.moveTo(ManualBackhaulBaselineStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualBackhaulBaselineStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualBackhaulBaseline("code-509", "label-586")
        item.status = ManualBackhaulBaselineStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualBackhaulBaselineStatus.PROPOSED) }
    }
}
