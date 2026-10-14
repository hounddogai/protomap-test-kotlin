package com.acme.transport.emissions.evcharge.pendingchargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultEvChargeScoreTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultEvChargeScore("code-572", "label-641")
        assertEquals(DefaultEvChargeScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultEvChargeScore("code-572", "label-641")
        try {
            item.moveTo(DefaultEvChargeScoreStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultEvChargeScoreStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultEvChargeScore("code-572", "label-641")
        item.status = DefaultEvChargeScoreStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultEvChargeScoreStatus.CREATED) }
    }
}
