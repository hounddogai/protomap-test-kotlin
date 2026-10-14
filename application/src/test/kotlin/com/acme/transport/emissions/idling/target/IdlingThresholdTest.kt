package com.acme.transport.emissions.idling.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingThresholdTest {
    @Test
    fun `starts in proposed`() {
        val item = newIdlingThreshold("code-907", "title-709")
        assertEquals(IdlingThresholdStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newIdlingThreshold("code-907", "title-709")
        try {
            item.moveTo(IdlingThresholdStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingThresholdStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newIdlingThreshold("code-907", "title-709")
        item.status = IdlingThresholdStatus.WITHDRAWN
        assertFails("move") { item.moveTo(IdlingThresholdStatus.PROPOSED) }
    }
}
