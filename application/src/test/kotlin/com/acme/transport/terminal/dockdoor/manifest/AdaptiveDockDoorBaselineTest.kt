package com.acme.transport.terminal.dockdoor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDockDoorBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newAdaptiveDockDoorBaseline("code-663", "title-646")
        assertEquals(AdaptiveDockDoorBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newAdaptiveDockDoorBaseline("code-663", "title-646")
        try {
            item.moveTo(AdaptiveDockDoorBaselineStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDockDoorBaselineStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAdaptiveDockDoorBaseline("code-663", "title-646")
        item.status = AdaptiveDockDoorBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(AdaptiveDockDoorBaselineStatus.PLANNED) }
    }
}
