package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireScoreTest {
    @Test
    fun `starts in new`() {
        val item = newTireScore("code-380", "title-717")
        assertEquals(TireScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newTireScore("code-380", "title-717")
        try {
            item.moveTo(TireScoreStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireScoreStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTireScore("code-380", "title-717")
        item.status = TireScoreStatus.FAILED
        assertFails("move") { item.moveTo(TireScoreStatus.NEW) }
    }
}
