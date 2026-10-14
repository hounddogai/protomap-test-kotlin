package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualIdlingTargetTest {
    @Test
    fun `starts in draft`() {
        val item = newManualIdlingTarget("code-695", "title-529")
        assertEquals(ManualIdlingTargetStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newManualIdlingTarget("code-695", "title-529")
        try {
            item.moveTo(ManualIdlingTargetStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualIdlingTargetStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newManualIdlingTarget("code-695", "title-529")
        item.status = ManualIdlingTargetStatus.CLOSED
        assertFails("move") { item.moveTo(ManualIdlingTargetStatus.DRAFT) }
    }
}
