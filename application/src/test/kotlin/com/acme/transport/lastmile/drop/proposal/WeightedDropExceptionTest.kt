package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDropExceptionTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedDropException("code-195", "title-495")
        assertEquals(WeightedDropExceptionStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedDropException("code-195", "title-495")
        try {
            item.moveTo(WeightedDropExceptionStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDropExceptionStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedDropException("code-195", "title-495")
        item.status = WeightedDropExceptionStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedDropExceptionStatus.REQUESTED) }
    }
}
