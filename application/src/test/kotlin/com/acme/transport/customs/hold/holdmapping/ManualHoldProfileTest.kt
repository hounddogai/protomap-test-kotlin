package com.acme.transport.customs.hold.holdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualHoldProfileTest {
    @Test
    fun `starts in created`() {
        val item = newManualHoldProfile("code-914", "label-216")
        assertEquals(ManualHoldProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualHoldProfile("code-914", "label-216")
        try {
            item.moveTo(ManualHoldProfileStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualHoldProfileStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualHoldProfile("code-914", "label-216")
        item.status = ManualHoldProfileStatus.RETIRED
        assertFails("move") { item.moveTo(ManualHoldProfileStatus.CREATED) }
    }
}
