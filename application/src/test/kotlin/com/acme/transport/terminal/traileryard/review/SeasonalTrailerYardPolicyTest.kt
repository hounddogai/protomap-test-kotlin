package com.acme.transport.terminal.traileryard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerYardPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalTrailerYardPolicy("code-986", "label-919")
        assertEquals(SeasonalTrailerYardPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newSeasonalTrailerYardPolicy("code-986", "label-919")
        try {
            item.moveTo(SeasonalTrailerYardPolicyStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerYardPolicyStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalTrailerYardPolicy("code-986", "label-919")
        item.status = SeasonalTrailerYardPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalTrailerYardPolicyStatus.REQUESTED) }
    }
}
