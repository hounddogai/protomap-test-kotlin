package com.acme.transport.customs.hold.fallbackholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHoldWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualHoldWindow("code-13", "name-767")
        assertEquals(ManualHoldWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualHoldWindow("code-13", "name-767")
        try {
            item.moveTo(ManualHoldWindowStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHoldWindowStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualHoldWindow("code-13", "name-767")
        item.status = ManualHoldWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualHoldWindowStatus.PROPOSED) }
    }
}
