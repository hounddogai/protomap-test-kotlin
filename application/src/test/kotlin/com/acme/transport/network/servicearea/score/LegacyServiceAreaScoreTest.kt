package com.acme.transport.network.servicearea.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyServiceAreaScoreTest {
    @Test
    fun `starts in new`() {
        val item = newLegacyServiceAreaScore("code-501", "label-906")
        assertEquals(LegacyServiceAreaScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newLegacyServiceAreaScore("code-501", "label-906")
        try {
            item.moveTo(LegacyServiceAreaScoreStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyServiceAreaScoreStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newLegacyServiceAreaScore("code-501", "label-906")
        item.status = LegacyServiceAreaScoreStatus.FAILED
        assertFails("move") { item.moveTo(LegacyServiceAreaScoreStatus.NEW) }
    }
}
