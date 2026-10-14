package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newNearMissScore("code-506", "name-927")
        assertEquals(NearMissScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newNearMissScore("code-506", "name-927")
        try {
            item.moveTo(NearMissScoreStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissScoreStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNearMissScore("code-506", "name-927")
        item.status = NearMissScoreStatus.STOPPED
        assertFails("move") { item.moveTo(NearMissScoreStatus.IDLE) }
    }
}
