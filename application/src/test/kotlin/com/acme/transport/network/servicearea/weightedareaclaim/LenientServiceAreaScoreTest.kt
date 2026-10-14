package com.acme.transport.network.servicearea.weightedareaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientServiceAreaScoreTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientServiceAreaScore("code-913", "title-448")
        assertEquals(LenientServiceAreaScoreStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientServiceAreaScore("code-913", "title-448")
        try {
            item.moveTo(LenientServiceAreaScoreStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientServiceAreaScoreStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientServiceAreaScore("code-913", "title-448")
        item.status = LenientServiceAreaScoreStatus.STOPPED
        assertFails("move") { item.moveTo(LenientServiceAreaScoreStatus.IDLE) }
    }
}
