package com.acme.transport.lastmile.locker.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLockerExceptionTest {
    @Test
    fun `starts in proposed`() {
        val item = newWeightedLockerException("code-648", "name-601")
        assertEquals(WeightedLockerExceptionStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newWeightedLockerException("code-648", "name-601")
        try {
            item.moveTo(WeightedLockerExceptionStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLockerExceptionStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newWeightedLockerException("code-648", "name-601")
        item.status = WeightedLockerExceptionStatus.WITHDRAWN
        assertFails("move") { item.moveTo(WeightedLockerExceptionStatus.PROPOSED) }
    }
}
