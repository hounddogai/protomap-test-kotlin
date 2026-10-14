package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CoachingVarianceTest {
    @Test
    fun `starts in new`() {
        val item = newCoachingVariance("code-285", "name-537")
        assertEquals(CoachingVarianceStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newCoachingVariance("code-285", "name-537")
        try {
            item.moveTo(CoachingVarianceStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CoachingVarianceStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newCoachingVariance("code-285", "name-537")
        item.status = CoachingVarianceStatus.FAILED
        assertFails("move") { item.moveTo(CoachingVarianceStatus.NEW) }
    }
}
