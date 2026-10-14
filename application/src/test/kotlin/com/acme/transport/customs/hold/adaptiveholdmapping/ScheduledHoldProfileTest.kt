package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldProfileTest {
    @Test
    fun `starts in created`() {
        val item = newScheduledHoldProfile("code-991", "name-648")
        assertEquals(ScheduledHoldProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScheduledHoldProfile("code-991", "name-648")
        try {
            item.moveTo(ScheduledHoldProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScheduledHoldProfile("code-991", "name-648")
        item.status = ScheduledHoldProfileStatus.RETIRED
        assertFails("move") { item.moveTo(ScheduledHoldProfileStatus.CREATED) }
    }
}
