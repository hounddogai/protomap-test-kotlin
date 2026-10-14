package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedIdlingTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedIdlingTarget("code-705", "label-605")
        assertEquals(CappedIdlingTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedIdlingTarget("code-705", "label-605")
        try {
            item.moveTo(CappedIdlingTargetStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedIdlingTargetStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedIdlingTarget("code-705", "label-605")
        item.status = CappedIdlingTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedIdlingTargetStatus.PROPOSED) }
    }
}
